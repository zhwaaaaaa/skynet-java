package com.zhw.skynet.core;

import com.zhw.skynet.common.RpcException;
import com.zhw.skynet.core.protocol.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Client {

    private static final EventLoopGroup GROUP = new NioEventLoopGroup(2);
    private static final InternalLogger log = InternalLoggerFactory.getInstance(Client.class);

    private final Codec<Request, Response> codec =
            new CompositeCodec<>(new RequestEncoder(), null);

    private ConcurrentHashMap<Integer, Request> map = new ConcurrentHashMap<>();

    private Bootstrap bootstrap;
    private Channel channel;

    public Client(String host, int port) {
        bootstrap = new Bootstrap();
        bootstrap.group(GROUP);
        bootstrap.remoteAddress(new InetSocketAddress(host, port));
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline().addLast(ShakeHandsHandler.forClient());
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

        Channel channel = bootstrap.connect().awaitUninterruptibly().channel();
        ShakeHandsReq handsReq = new ShakeHandsReq(services);
        ChannelFuture future = channel.write(handsReq);
        future.awaitUninterruptibly();
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
        ReqAction action = new ReqAction(buf);
        channel.write(action);
        ByteBuf byteBuf;
        try {
            byteBuf = action.waitResponse(req.getTimeout());
        } catch (Throwable e) {
            if (e instanceof RpcException) {
                throw (RpcException) e;
            }
            throw new RpcException(e);
        }
        if (byteBuf == null && action.getBodyLen() != -1) {
            throw new RpcException("timeout after[ms] " + 1000);
        }
        return null;
    }

    public void sendAsync(Request req, Consumer<Response> resp) {

    }

    public void close() {
        Channel ch = this.channel;
        if (ch != null) {
            ch.close().awaitUninterruptibly();
        }
    }
}
