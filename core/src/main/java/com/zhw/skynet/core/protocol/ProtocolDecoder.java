package com.zhw.skynet.core.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class ProtocolDecoder extends ChannelDuplexHandler {
    static final String HANDLER_NAME = "Codec";
    private static final InternalLogger log = InternalLoggerFactory.getInstance(ProtocolDecoder.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        log.info("received message {} bytes", buf.readableBytes());
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        promise.addListener(f -> {
            if (!f.isSuccess()) {
                log.error("write msg ", f.cause());
            }
        });
        ReqAction reqAction = (ReqAction) msg;
        super.write(ctx, reqAction.fetchReqBuf(), promise);
    }
}
