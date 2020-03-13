package com.zhw.skynet.transfer;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.common.ServiceCount;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.List;

public abstract class TransferHandler extends ByteToMessageCodec<List<ServiceCount>> {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(TransferHandler.class);

    private static final int STATE_INIT = 0;
    private static final int STATE_SHAKING = 1;
    private static final int STATE_TRANSFER = 2;

    public TransferHandler(ServiceRegisterer registerer) {
        this.registerer = registerer;

    }

    private int status = STATE_INIT;
    private ServiceRegisterer registerer;

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return !(msg instanceof ByteBuf);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, List<ServiceCount> msg, ByteBuf out) throws Exception {
        out.writeZero(4);
        out.writeShortLE(msg.size());
        int len = 2;
        for (ServiceCount count : msg) {
            int length = count.getServiceName().length();
            out.writeByte(length);
            out.writeCharSequence(count.getServiceName(), Constants.UTF8);
            out.writeByte(count.getCount());
            len += length + 2;
        }
        out.setIntLE(0, len);
        status = STATE_TRANSFER;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (status) {
            case STATE_INIT:
                doShakeHands(ctx, in, out);
                break;
            case STATE_TRANSFER:
                doTransfer(ctx, in, out);
                break;
            default:
                log.info("decode not complete,but send data.close channel");
                ctx.channel().close();
                break;
        }
    }

    protected abstract void doTransfer(ChannelHandlerContext ctx, ByteBuf in, List<Object> out);


    private int shakePkgLen;

    private void doShakeHands(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
        int serviceSize;
        Channel channel = ctx.channel();
        if (registerer == null) {
            if (buf.readableBytes() < 7) {
                return;
            }
            if ((buf.readByte() & 0xFF) != Constants.MSG_TYPE_SERVER_SHAKE) {
                log.info("error msg type,expect {}", Constants.MSG_TYPE_SERVER_SHAKE);
                channel.close();
                return;
            }
            shakePkgLen = buf.readIntLE() - 2;

            serviceSize = buf.readShortLE() & 0xFFFF;
            if (serviceSize <= 0) {
                log.info("closed no service channel {}", channel.remoteAddress());
                channel.close();
                return;
            }

            registerer = new ServiceRegisterer(serviceSize);
        } else {
            serviceSize = registerer.getServiceSize();
        }

        if (buf.readableBytes() < shakePkgLen) {
            return;
        }

        int decodeLen = 0;

        for (int i = 0; i < serviceSize; i++) {
            int servLen = buf.readByte() & 0xFF;
            if (servLen > Constants.MAX_SERVICE_LEN) {
                log.info("close channel because of invalid service len", servLen);
                channel.close();
                return;
            }
            decodeLen += servLen + 1;
            if (invalidLen(channel, decodeLen)) {
                return;
            }
            String service = buf.readCharSequence(servLen, Constants.UTF8).toString();

            int paramLen = buf.readIntLE();
            decodeLen += paramLen + 4;
            if (invalidLen(channel, decodeLen)) {
                return;
            }
            String param = buf.readCharSequence(paramLen, Constants.UTF8).toString();

            int resultLen = buf.readIntLE();
            decodeLen += paramLen + 4;
            if (invalidLen(channel, decodeLen)) {
                return;
            }
            String result = buf.readCharSequence(resultLen, Constants.UTF8).toString();
            ServiceDesc desc = new ServiceDesc(service, 0, param, result);
            registerer.addService(desc);
        }

        registerer.registerAll(channel::writeAndFlush);
        status = STATE_SHAKING;
    }

    private boolean invalidLen(Channel channel, int decodeLen) {
        if (decodeLen >= shakePkgLen) {
            log.info("valid shake hands package");
            channel.close();
            return true;
        }
        return false;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        registerer.destroy();
        super.channelInactive(ctx);
    }
}
