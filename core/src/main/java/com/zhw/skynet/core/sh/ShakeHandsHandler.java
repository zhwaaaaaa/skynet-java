package com.zhw.skynet.core.sh;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.core.MsgType;
import com.zhw.skynet.core.ServiceMeta;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;

public class ShakeHandsHandler extends ChannelDuplexHandler {

    public static ShakeHandsHandler forProvider() {
        return new ShakeHandsHandler(MsgType.MT_PROVIDER_SH);
    }

    public static ShakeHandsHandler forConsumer() {
        return new ShakeHandsHandler(MsgType.MT_CONSUMER_SH);
    }

    private ShakeHandsReq req;
    private final MsgType shakeType;


    protected ShakeHandsHandler(MsgType shakeType) {
        this.shakeType = shakeType;
    }

    protected ByteBuf encode(ChannelHandlerContext ctx, ShakeHandsReq req) {
        ByteBuf alloc = ctx.alloc().buffer();
        alloc.writeByte(shakeType.val());
        int sizeIndex = alloc.writerIndex();
        alloc.writeZero(4);
        int size = req.getServices().size();
        alloc.writeShortLE(size);
        int len = 2;
        for (ServiceMeta service : req.getServices()) {
            int length = service.getServiceName().length();
            alloc.writeByte(length);
            alloc.writeCharSequence(service.getServiceName(), Constants.UTF8);
            len += length + 1;
        }
        alloc.setIntLE(sizeIndex, len);
        return alloc;
    }

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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf in = (ByteBuf) msg;
            MsgType msgType = MsgType.from(in.readByte() & 0xFF);
            if (msgType != MsgType.MT_SH_RESP) {
                req.notifyError(new ShakeHandsException("expect receive shake hands response get "
                        + msgType));
                ctx.close();
                return;
            }
            int packageLen = in.readIntLE();
            int serviceSize = in.readUnsignedShortLE();
            if (serviceSize != req.getServices().size()) {
                req.notifyError(new ShakeHandsException("expect service size "
                        + req.getServices().size() + ",get " + serviceSize));
                ctx.close();
                return;
            }
            ShakeHandsResp response = new ShakeHandsResp(new ArrayList<>(serviceSize));
            // 除了packageLen 的长度，已经读了2个字节了
            int bytesToDecode = packageLen - 2;
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
                response.addServiceCount(sequence.toString(), in.readByte() & 0xFF);
                bytesToDecode -= servLen + 2;
            } while (bytesToDecode > 0);
            if (bytesToDecode != 0 || in.readableBytes() != 0) {
                req.notifyError(new ShakeHandsException("valid shake hands response"));
            } else {
                ctx.pipeline().remove(this);
                req.notifyResp(response);
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
