package com.zhw.skynet.transfer;

import com.zhw.skynet.common.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.HashedWheelTimer;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RemoteServerConnection implements RemoteEndPoint {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(RemoteServerConnection.class);

    private Set<String> services = new HashSet<>();

    private final HashedWheelTimer timer;
    private final RemoteServiceManager remoteServiceManager;
    private final Bootstrap bootstrap;
    private final String ip;
    private final int port;

    private Channel ch;
    private volatile boolean userClose;

    public RemoteServerConnection(HashedWheelTimer timer,
                                  RemoteServiceManager remoteServiceManager,
                                  String ip, int port) {
        this.timer = timer;
        this.port = port;
        this.ip = ip;
        this.remoteServiceManager = remoteServiceManager;
        this.bootstrap = new Bootstrap();
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline()
                        .addLast("slicePackageHandler",
                                new LengthFieldBasedFrameDecoder(
                                        ByteOrder.LITTLE_ENDIAN,
                                        Constants.MAX_MSG_LEN,
                                        1,
                                        4,
                                        0,
                                        0,
                                        true
                                ))
                        .addLast(new ResponseTransferHandler());
            }
        });
    }

    public void connect() {
        if (!userClose) {
            ChannelFuture future = bootstrap.connect(ip, port);
            SocketAddress address = future.channel().remoteAddress();
            future.addListener(f -> {
                if (!f.isSuccess()) {
                    timer.newTimeout(t -> connect(), 2000, TimeUnit.MILLISECONDS);
                    log.warn("error connect to {} because of {}", address, f.cause());
                }
            });
        }
    }

    public boolean matchIpPort(String ip, int port) {
        return this.port == port && this.ip.equals(ip);
    }

    public class ResponseTransferHandler extends ValidHeaderHandler {

        @Override
        protected boolean handleResp(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            int chId = in.getIntLE(in.readerIndex() + 9);
            RemoteEndPoint receiver = remoteServiceManager.getResponseReceiver(chId);
            if (receiver == null) {
                return false;
            }
            return receiver.send(in);
        }


        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ch = ctx.channel();
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            ch = null;
            super.channelInactive(ctx);
            timer.newTimeout(t -> RemoteServerConnection.this.connect(), 2000, TimeUnit.MILLISECONDS);
        }

    }

    @Override
    public boolean send(ByteBuf buf) {
        if (ch != null) {
            ch.writeAndFlush(buf);
            return true;
        }
        return false;
    }

    public void addRelateService(String service) {
        services.add(service);
    }

    public int removeRelateService(String service) {
        services.remove(service);
        return services.size();
    }

    @Override
    public Set<String> relateServices() {
        return services;
    }

    @Override
    public void close() {
        userClose = true;
        if (ch != null) {
            ch.close();
        }

    }

}
