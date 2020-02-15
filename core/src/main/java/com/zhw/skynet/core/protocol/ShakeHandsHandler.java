package com.zhw.skynet.core.protocol;

import com.zhw.skynet.common.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

public abstract class ShakeHandsHandler extends ByteToMessageDecoder implements ChannelOutboundHandler {
    private List<ShakeHandsReq.ServiceCount> response;
    private ShakeHandsReq req;
    private int packageLen;

    protected abstract int connType();

    protected abstract ByteBuf encode(ChannelHandlerContext ctx, ShakeHandsReq req);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ShakeHandsReq) {
            try {
                ByteBuf buf = encode(ctx, req = (ShakeHandsReq) msg);
                ctx.write(buf, promise);
            } catch (Throwable e) {
                promise.setFailure(e);
            }
        } else {
            ctx.write(msg, promise);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (response == null) {
            if (in.readableBytes() < 6) {
                return;
            }
            packageLen = in.readIntLE();
            int serviceSize = in.readShortLE();
            if (serviceSize != req.getServices().size()) {
                req.notifyError(new ShakeHandsException("expect service size "
                        + req.getServices().size() + ",get " + serviceSize));
                return;
            }
            response = new ArrayList<>(serviceSize);
        }
        // 除了packageLen 的长度，已经读了2个字节了
        int bytesToDecode = packageLen - 2;
        if (in.readableBytes() < bytesToDecode) {
            return;
        }
        do {
            int servLen = in.readByte() & 0xFF;
            if (servLen > Constants.CODE_LOCAL_ERROR) {
                req.notifyError(new ShakeHandsException("max service name length "
                        + Constants.CODE_LOCAL_ERROR + ",get " + servLen));
                return;
            }
            if (in.readableBytes() < servLen + 1) {
                req.notifyError(new ShakeHandsException("valid shake hands response"));
                return;
            }
            CharSequence sequence = in.readCharSequence(servLen, Constants.UTF8);
            response.add(new ShakeHandsReq.ServiceCount(sequence.toString(), in.readByte() & 0xFF));
            bytesToDecode -= servLen + 2;
        } while (bytesToDecode > 0);
        if (bytesToDecode != 0) {
            req.notifyError(new ShakeHandsException("valid shake hands response"));
        } else {
            ctx.pipeline().replace(this, "transferHandler", getTransferHandler());
            req.notifyResp(response);
            if (in.readableBytes() > 0) {
                ctx.fireChannelRead(in);
            }
        }
    }

    @Override
    public final void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (req != null) {
            req.notifyError(new ClosedChannelException());
            req = null;
        }
        ctx.close();
    }


    @Override
    public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (req != null) {
            req.notifyError(new ClosedChannelException());
            req = null;
        }
    }

    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (req != null) {
            req.notifyError(cause);
            req = null;
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    protected abstract ChannelHandler getTransferHandler();

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }
}
