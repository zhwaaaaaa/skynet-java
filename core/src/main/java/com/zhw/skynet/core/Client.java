package com.zhw.skynet.core;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.common.RpcException;
import com.zhw.skynet.core.protocol.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Client implements EndPoint {

    private static final EventLoopGroup GROUP = new NioEventLoopGroup(2);
    private static final InternalLogger log = InternalLoggerFactory.getInstance(Client.class);

    private class ResponseReceiveHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof ResponseMessage) {
                ResponseMessage response = (ResponseMessage) msg;
                ReqAction reqAction = map.remove(response.getResponseCode());
                if (reqAction != null && reqAction.notifyResp(response)) {
                    return;
                }
            }
            super.channelRead(ctx, msg);
        }
    }

    private ConcurrentHashMap<Integer, ReqAction> map = new ConcurrentHashMap<>();
    private HashedWheelTimer timer = new HashedWheelTimer();
    private Encoder<Request> encoder = new RequestEncoder();

    private Bootstrap bootstrap;
    private Channel channel;

    private Executor executor;


    public Client(String host, int port, Executor asyncExecutor) {
        bootstrap = new Bootstrap();
        bootstrap.group(GROUP);
        bootstrap.remoteAddress(new InetSocketAddress(host, port));
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ClientShakeHandsHandler())
                        .addLast("ResponseReceiveHandler", new ResponseReceiveHandler());
            }
        });
        executor = asyncExecutor;
    }

    public void start(Collection<ServiceMeta> serviceMetas) throws Throwable {
        EndPoint.validServiceMetas(serviceMetas);

        ChannelFuture f = bootstrap.connect().awaitUninterruptibly();
        if (!f.isSuccess()) {
            throw f.cause();
        }

        Channel channel = f.channel();
        ShakeHandsReq handsReq = new ShakeHandsReq(serviceMetas);
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
        long timeoutMs = req.getMeta().getTimeoutMs();
        ResponseMessage msg = null;
        Throwable err = null;
        try {
            ByteBuf buf = encoder.encode(req, req.getMeta());
            ReqAction action = new ReqAction(req);
            map.put(req.getReqId(), action);
            channel.writeAndFlush(buf);
            msg = action.waitResponse(timeoutMs);
            if (msg == null) {
                map.remove(req.getReqId());
                err = new RpcException("timeout after[ms] " + timeoutMs);
            }
        } catch (RpcException e) {
            err = e;
        } catch (Throwable e) {
            err = new RpcException(e);
        }
        return convertToResponse(req, msg, err);
    }

    private Response convertToResponse(Request req, ResponseMessage msg, Throwable err) {
        Response response = new Response();
        response.setReqId(req.getReqId());
        if (err != null) {
            response.setCode(Constants.CODE_LOCAL_ERROR);
            response.setErr(err);
        } else {
            response.setCode(msg.getResponseCode());
            response.setClientId(msg.getClientId());
            response.setServerId(msg.getServerId());
            response.setBodyType(msg.getBodyType());

            if (msg.getBodyLen() > 0) {
                // TODO 判断不同的bodyType 用不同的序列化方式,这里先判断code是否正常
                if (response.getCode() == 0) {
                    response.setBody(req.getMeta().getResponseMapper().read(new ByteBufInputStream(msg.getBodyBuf())));
                } else {
                    response.setBody(msg.getBodyBuf().toString(Constants.UTF8));
                }
            }
            msg.release();
        }
        return response;
    }

    public void sendAsync(Request req, BiConsumer<Response, Throwable> consumer) {
        ByteBuf buf = encoder.encode(req, req.getMeta());
        long timeoutMs = req.getMeta().getTimeoutMs();
        Timeout timeout = timer.newTimeout(t -> {
            if (!t.isCancelled()) {
                ReqAction action = map.remove(req.getReqId());
                if (action != null) {
                    action.notifyError(new RpcException("timeout after[ms] "
                            + timeoutMs));
                }
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);
        ReqAction action = new ReqAction(req, (r, e) -> {
            timeout.cancel();
            executor.execute(() -> consumer.accept(convertToResponse(req, r, e), e));
        });
        map.put(req.getReqId(), action);
        channel.writeAndFlush(buf);
    }

    @Override
    public void close() {
        Channel ch = this.channel;
        if (ch != null) {
            ch.close().awaitUninterruptibly();
        }
    }
}
