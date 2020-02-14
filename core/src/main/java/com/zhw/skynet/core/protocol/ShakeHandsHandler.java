package com.zhw.skynet.core.protocol;

import com.zhw.skynet.common.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

public abstract class ShakeHandsHandler extends ChannelDuplexHandler {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(ShakeHandsHandler.class);

    private List<ShakeHandsReq.ServiceCount> response;
    private ShakeHandsReq req;
    private ByteBuf initBuf;
    private int responseLen = -1;
    private int serviceSize = -1;
    private int serviceLen = -1; // serviceLen

    protected abstract int connType();

    protected abstract ByteBuf encode(ChannelHandlerContext ctx, ShakeHandsReq req);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ShakeHandsReq) {
            ByteBuf buf = encode(ctx, (ShakeHandsReq) msg);
            super.write(ctx, buf, promise);
        } else {
            super.write(ctx, msg, promise);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            assert req != null;

            ByteBuf buf = (ByteBuf) msg;
            try {
                tryDecode(ctx, buf);
            } catch (Throwable e) {
                req.notifyError(e);
                releaseInitBuf();
                log.error("error decode shake hands ", e);
                ctx.channel().close();
                req = null;
            } finally {
                buf.release();
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    private void tryDecode(ChannelHandlerContext ctx, ByteBuf buf) {
        if (initBuf == null) {
            initBuf = ctx.alloc().buffer(64);
        }
        int i = buf.readableBytes();
        if (responseLen <= 0) {
            int leftBytes = initBuf.readableBytes();
            // 还没有解析responseLen
            if (i + leftBytes < 6) {
                // 还不够解析responseLen
                initBuf.writeBytes(buf);
                return;
            } else if (leftBytes > 0) {
                // 之前读了几个字节
                initBuf.writeBytes(buf, 6 - leftBytes);
                readResponseLen(initBuf);
                // initBuf 必须清空
                initBuf.writerIndex(0);
                initBuf.readerIndex(0);
            } else {
                readResponseLen(buf);
            }
            if (buf.readableBytes() == 0) {
                return;
            }
        }
        // 剩下的试着解析service
        boolean complete = tryDecodeServices(buf);
        if (complete) {
            releaseInitBuf();
            req.notifyResp(response);
            req = null;
            response = null;
            ctx.pipeline().replace(this, "transferDecoder", getTransferHandler());
        }
    }

    protected abstract ChannelHandler getTransferHandler();

    private boolean tryDecodeServices(ByteBuf buf) {

        do {
            if (serviceLen <= 0) {
                int current = buf.readableBytes();
                int left = initBuf.readableBytes();
                if (left + current < 2) {
                    initBuf.writeBytes(buf);
                    break;
                } else if (left > 0) {
                    initBuf.writeByte(buf.readByte());
                    readServiceLen(initBuf);
                    // initBuf 必须清空
                    initBuf.writerIndex(0);
                    initBuf.readerIndex(0);
                } else {
                    readServiceLen(buf);
                }
                if (buf.readableBytes() == 0) {
                    break;
                }
            }
        } while (tryReadServiceName(buf));
        // 检查是否吧所有的service都读了
        int size = response.size();
        if (size < serviceSize) {
            return false;
        }

        if (size > serviceSize) {
            throw new ShakeHandsException("found service " + size + " expected " + serviceSize);
        }
        if (initBuf.readableBytes() != 0 || buf.readableBytes() != 0) {
            // 包都解析完了。应该没有数据没有读了
            throw new ShakeHandsException("read completed buf found data left");
        }

        return true;
    }

    private boolean tryReadServiceName(ByteBuf buf) {
        int current = buf.readableBytes();
        int left = initBuf.readableBytes();
        if (current + left < serviceLen + 1) {
            initBuf.writeBytes(buf);
            return false;
        } else if (left > 0) {
            initBuf.writeBytes(buf, serviceLen + 1 - left);
            readServiceName(initBuf);
            // initBuf 必须清空
            initBuf.writerIndex(0);
            initBuf.readerIndex(0);
        } else {
            readServiceName(buf);
        }
        return buf.readableBytes() > 0;
    }

    private void readServiceName(ByteBuf buf) {
        // 这个不会改变读指针
        String s = buf.toString(buf.readerIndex(), serviceLen, Constants.UTF8);
        buf.readerIndex(buf.readerIndex() + serviceLen);

        response.add(new ShakeHandsReq.ServiceCount(s, buf.readByte() & 0xFF));
        serviceLen = -1;
    }

    private void readServiceLen(ByteBuf buf) {
        serviceLen = buf.readByte() & 0xFF;
        if (serviceLen > Constants.MAX_SERVICE_LEN) {
            throw new ShakeHandsException("max service len: " + Constants.MAX_SERVICE_LEN +
                    " found " + serviceLen);
        }

    }

    private void readResponseLen(ByteBuf buf) {
        // 读两个长度。
        // 第一次发了一个字节数 >= 8的包回来
        responseLen = buf.readIntLE();
        serviceSize = buf.readShortLE();
        if (serviceSize != req.getServices().size()) {
            throw new ShakeHandsException("error serviceSize " + serviceSize +
                    "," + req.getServices().size() + " expected");
        }
        response = new ArrayList<>(serviceSize);
    }

    @Override
    public final void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        releaseInitBuf();
        if (req != null) {
            req.notifyError(new ClosedChannelException());
            req = null;
        }
        super.close(ctx, promise);
    }

    private void releaseInitBuf() {
        if (initBuf != null) {
            initBuf.release();
            initBuf = null;
        }
    }

    @Override
    public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
        releaseInitBuf();
        if (req != null) {
            req.notifyError(new ClosedChannelException());
            req = null;
        }
    }

    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        releaseInitBuf();
        if (req != null) {
            req.notifyError(cause);
            req = null;
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


}
