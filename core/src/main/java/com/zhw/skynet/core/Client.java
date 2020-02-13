package com.zhw.skynet.core;

import com.zhw.skynet.common.RpcException;
import com.zhw.skynet.core.protocol.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Client {

    private static final EventLoopGroup GROUP = new NioEventLoopGroup(2);
    private static final InternalLogger log = InternalLoggerFactory.getInstance(Client.class);

    private class ResponseReceiveHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Response) {
                Response response = (Response) msg;
                ReqAction reqAction = map.remove(response.getReqId());
                if (reqAction == null) {
                    super.channelRead(ctx, msg);
                    return;
                }
                if (!reqAction.notifyResp(response)) {
                    super.channelRead(ctx, msg);
                    return;
                }
            } else {
                super.channelRead(ctx, msg);
            }
        }
    }

    private final Codec<Request, Response> codec =
            new CompositeCodec<>(new RequestEncoder(), null);

    private ConcurrentHashMap<Integer, ReqAction> map = new ConcurrentHashMap<>();
    private HashedWheelTimer timer = new HashedWheelTimer();

    private Bootstrap bootstrap;
    private Channel channel;

    public Client(String host, int port) {
        bootstrap = new Bootstrap();
        bootstrap.group(GROUP);
        bootstrap.remoteAddress(new InetSocketAddress(host, port));
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline().addLast(ShakeHandsHandler.forClient())
                        .addLast("ResponseReceiveHandler", new ResponseReceiveHandler());
            }
        });
    }

    public void start(Collection<String> requireServices) throws Throwable {
        if (CollectionUtils.isEmpty(requireServices)) {
            throw new IllegalArgumentException("requireServices required");
        }
        HashSet<String> services = new HashSet<>(requireServices);

        if (services.size() > ShakeHandsHandler.MAX_SERVICE_SIZE) {
            throw new IllegalArgumentException("requireServices max size " + ShakeHandsHandler.MAX_SERVICE_SIZE);
        }

        ChannelFuture f = bootstrap.connect().awaitUninterruptibly();
        if (!f.isSuccess()) {
            throw f.cause();
        }

        Channel channel = f.channel();
        ShakeHandsReq handsReq = new ShakeHandsReq(services);
        channel.writeAndFlush(handsReq).awaitUninterruptibly();

        List<ShakeHandsReq.ServiceCount> list = handsReq.waitResponse(30000);
        if (list == null) {
            channel.close().awaitUninterruptibly();
            throw new ShakeHandsException("timeout");
        }

        if (log.isDebugEnabled()) {
            for (ShakeHandsReq.ServiceCount count : list) {
                log.debug("found {}", count);
            }
        }

        List<String> notFound = list.stream().filter(x -> x.getProviderCount() == 0)
                .map(ShakeHandsReq.ServiceCount::getName)
                .collect(Collectors.toList());
        if (notFound.size() > 0) {
            log.error("not found service {}", notFound);
            throw new ShakeHandsException("service not found " + notFound);
        }

        this.channel = channel;
    }

    public Response send(Request req) throws RpcException {
        ByteBuf buf = codec.encode(req);
        ReqAction action = new ReqAction(req);
        map.put(req.getReqId(), action);
        channel.writeAndFlush(buf);
        Response response;
        try {
            response = action.waitResponse(req.getTimeout());
        } catch (Throwable e) {
            if (e instanceof RpcException) {
                throw (RpcException) e;
            }
            throw new RpcException(e);
        }
        if (response == null) {
            map.remove(req.getReqId());
            throw new RpcException("timeout after[ms] " + req.getTimeout());
        }
        return response;
    }

    public void sendAsync(Request req, BiConsumer<Response, Throwable> consumer) {
        ByteBuf buf = codec.encode(req);
        Timeout timeout = timer.newTimeout(t -> {
            if (!t.isCancelled()) {
                ReqAction action = map.remove(req.getReqId());
                if (action != null) {
                    action.notifyError(new RpcException("timeout after[ms] "
                            + req.getTimeout()));
                }
            }
        }, req.getTimeout(), TimeUnit.MILLISECONDS);
        ReqAction action = new ReqAction(req, (r, e) -> {
            timeout.cancel();
            consumer.accept(r, e);
        });
        map.put(req.getReqId(), action);
        channel.writeAndFlush(buf);
    }

    public void close() {
        Channel ch = this.channel;
        if (ch != null) {
            ch.close().awaitUninterruptibly();
        }
    }
}
