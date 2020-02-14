package com.zhw.skynet.core.protocol;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.core.ServiceMeta;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.Collection;

public class ClientShakeHandsHandler extends ShakeHandsHandler {

    public static final int CONN_CLIENT = 0;

    public ClientShakeHandsHandler() {
    }


    @Override
    protected int connType() {
        return CONN_CLIENT;
    }

    @Override
    protected ByteBuf encode(ChannelHandlerContext ctx, ShakeHandsReq req) {
        Collection<ServiceMeta> services = req.getServices();
        ByteBuf buffer = ctx.channel().alloc().buffer(services.size() * 32);
        // msgSize placeholder
        buffer.writeZero(4);
        buffer.writeByte(connType());
        buffer.writeShortLE(services.size());

        int msgSize = 3;
        for (ServiceMeta service : services) {
            int length = service.getServiceName().length();
            buffer.writeByte(length);
            buffer.writeCharSequence(service.getServiceName(), Constants.UTF8);
            msgSize += length + 1;
        }
        int i = buffer.writerIndex();
        buffer.writerIndex(0);
        buffer.writeIntLE(msgSize);
        buffer.writerIndex(i);
        return buffer;
    }

    @Override
    protected ChannelHandler getTransferHandler() {
        return new ResponseHandler();
    }
}
