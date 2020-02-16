package com.zhw.skynet.core.protocol;

import com.zhw.skynet.common.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;

import java.util.List;

public class RequestMessageHandler extends ByteToMessageDecoder {
    private RequestMessage msg;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (msg == null) {
            int serviceNameLen = in.readByte() & 0xFF;
            if (serviceNameLen > Constants.MAX_SERVICE_LEN) {
                throw new DecoderException("get service len = " + serviceNameLen);
            }
            msg = new RequestMessage(serviceNameLen);
        }
        int bodyLen;
        if (msg.getService() == null) {
            // [1:serviceLen][0-238:serviceName][4:requestId][4:clientId][4:serverId][1:bodyType][4:bodyLen][0-4G:data]
            int serviceLen = msg.getServiceLen();
            if (in.readableBytes() < serviceLen + 17) {
                return;
            }
            msg.setService(in.readCharSequence(serviceLen, Constants.UTF8).toString());
            msg.setRequestId(in.readIntLE());
            msg.setClientId(in.readIntLE());
            msg.setServerId(in.readIntLE());
            msg.setBodyType(in.readByte() & 0xFF);
            bodyLen = in.readIntLE();
            if (bodyLen == 0) {
                out.add(msg);
                msg = null;
                return;
            }
            msg.setBodyLen(bodyLen);
        } else {
            bodyLen = msg.getBodyLen();
        }
        if (in.readableBytes() < bodyLen) {
            return;
        }
        ByteBuf buf = in.retainedSlice(in.readerIndex(), bodyLen);
        // 跳过这个body。把它交给业务线程去解析
        in.skipBytes(bodyLen);
        msg.setBodyBuf(buf);
        out.add(msg);
        msg = null;
    }
}
