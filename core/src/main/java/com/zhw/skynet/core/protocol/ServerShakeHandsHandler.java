package com.zhw.skynet.core.protocol;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.core.ServiceMeta;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

public class ServerShakeHandsHandler extends ShakeHandsHandler {
    private static final int CONN_SERVER = 0xFF;

    @Override
    protected int connType() {
        return CONN_SERVER;
    }

    @Override
    protected ByteBuf encode(ChannelHandlerContext ctx, ShakeHandsReq req) {
        ByteBuf buffer = ctx.alloc().buffer(256);
        // packageLen
        buffer.writeZero(4);
        buffer.writeByte(CONN_SERVER);
        buffer.writeShortLE(req.getServices().size());
        int pkgSize = 3; // 1connType + 2serviceSize
        for (ServiceMeta meta : req.getServices()) {
            byte[] serv = meta.getServiceName().getBytes(Constants.UTF8);
            byte[] param = meta.getRequestMapper().typeDesc().getBytes(Constants.UTF8);
            byte[] result = meta.getResponseMapper().typeDesc().getBytes(Constants.UTF8);
            int serviceMetaLen = 1 + serv.length + 4 + param.length + 4 + result.length;
            buffer.writeByte(serv.length);
            buffer.writeBytes(serv);
            buffer.writeIntLE(param.length);
            buffer.writeBytes(param);
            buffer.writeIntLE(result.length);
            buffer.writeBytes(result);
            // 4serviceMetaLen + serviceMeta
            pkgSize += serviceMetaLen;
        }
        int wi = buffer.writerIndex();
        buffer.writerIndex(0);
        buffer.writeIntLE(pkgSize);
        buffer.writerIndex(wi);
        return buffer;
    }

    @Override
    protected ChannelHandler getTransferHandler() {
        return new RequestMessageHandler();
    }

}
