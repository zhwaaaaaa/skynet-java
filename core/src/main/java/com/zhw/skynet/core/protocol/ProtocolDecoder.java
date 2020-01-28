package com.zhw.skynet.core.protocol;

import io.netty.channel.*;

public class ProtocolDecoder extends ChannelDuplexHandler {
    static final String HANDLER_NAME = "Codec";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }
}
