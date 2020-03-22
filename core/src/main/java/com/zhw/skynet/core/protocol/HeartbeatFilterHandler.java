package com.zhw.skynet.core.protocol;

import com.zhw.skynet.core.MsgType;
import com.zhw.skynet.core.client.Client;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public abstract class HeartbeatFilterHandler extends ChannelInboundHandlerAdapter {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(Client.class);

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf in = (ByteBuf) msg;
            MsgType type = MsgType.from(in.getByte(0));
            if (type == null) {
                if (log.isDebugEnabled()) {
                    log.debug("close channel because of invalid message");
                }
                ctx.close();
            } else if (type != MsgType.MT_HEARTBEAT_RESP) {
                if (onMessage(type, in)) {
                    return;
                } else {
                    ctx.close();
                }
            }
        }
        super.channelRead(ctx, msg);
    }

    /**
     * @param type msgType
     * @param in   buf
     * @return true channel will be close
     * @throws Exception exception
     */
    protected abstract boolean onMessage(MsgType type, ByteBuf in) throws Exception;

    @Override
    public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ByteBuf buf = ctx.alloc().buffer(5);
            buf.writeByte(MsgType.MT_HEARTBEAT_REQ.val());
            buf.writeZero(4);
            ctx.writeAndFlush(buf);
        } else {
            super.channelRead(ctx, evt);
        }
    }
}
