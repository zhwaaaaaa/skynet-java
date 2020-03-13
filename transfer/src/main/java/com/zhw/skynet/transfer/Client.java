package com.zhw.skynet.transfer;

import com.zhw.skynet.common.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;

public class Client implements Transfer {

    private static final AtomicInteger CHANN_ID = new AtomicInteger();
    private ServerBootstrap appIn;
    private RemoteServiceManager remoteServiceManager;


    public void init(EventLoopGroup group) throws Exception {



        appIn = new ServerBootstrap();
        appIn.group(group);
        appIn.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
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
                        .addLast("transferHandler", new ClientRequestHandler(remoteServiceManager
                                , CHANN_ID.incrementAndGet()));
            }
        });
    }

    public void close() throws Exception {
        remoteServiceManager.close();
        appIn.clone();
    }

}
