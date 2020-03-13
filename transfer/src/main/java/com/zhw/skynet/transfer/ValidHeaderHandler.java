package com.zhw.skynet.transfer;

import com.zhw.skynet.common.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public abstract class ValidHeaderHandler extends ChannelDuplexHandler {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(ValidHeaderHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf in = (ByteBuf) msg;
            int code = in.getUnsignedByte(in.readerIndex());
            boolean cons;
            switch (code) {
                case Constants.MSG_TYPE_SERVER_SHAKE:
                case Constants.MSG_TYPE_CLIENT_SHAKE:
                    cons = handleShakeHands(code, ctx, in);
                    break;
                case Constants.MSG_TYPE_TRANSFER_REQ:
                    cons = handleReq(ctx, in);
                    break;
                case Constants.MSG_TYPE_TRANSFER_RESP:
                    cons = handleResp(ctx, in);
                    break;
                case Constants.MSG_TYPE_HEARTBEAT:
                    cons = handleHeartbeat(ctx, in);
                    break;
                default:
                    cons = closeChannel(code, ctx, in);
                    break;
            }
            if (!cons) {
                super.channelRead(ctx, msg);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    protected final boolean closeChannel(int code, ChannelHandlerContext ctx, ByteBuf in) {
        log.info("invalid code {} in channel {},to be close", code, ctx.channel().remoteAddress());
        ctx.close();
        return false;
    }

    protected boolean handleHeartbeat(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ctx.write(in);
        return true;
    }

    protected boolean handleShakeHands(int code, ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        return closeChannel(code, ctx, in);
    }

    protected boolean handleReq(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        return closeChannel(Constants.MSG_TYPE_TRANSFER_RESP, ctx, in);
    }

    protected boolean handleResp(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        return closeChannel(Constants.MSG_TYPE_TRANSFER_RESP, ctx, in);
    }
}
