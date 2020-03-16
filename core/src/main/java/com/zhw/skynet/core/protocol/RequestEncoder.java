package com.zhw.skynet.core.protocol;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.core.Request;
import com.zhw.skynet.core.ServiceMeta;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class RequestEncoder implements Encoder<Request> {
    private final ByteBufAllocator allocator;

    public RequestEncoder() {
        this(ByteBufAllocator.DEFAULT);
    }

    public RequestEncoder(ByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    @Override
    public ByteBuf encode(Request req, ServiceMeta meta) throws CodecException {
        //|1|4reqId|4clientId|4serverId|1|service|1|method|4bodyLen|body|
        ByteBuf buffer = allocator.buffer();
        try {
            encode0(buffer, req, req.getMeta());
        } catch (Throwable e) {
            buffer.release();
            if (e instanceof CodecException) {
                throw e;
            }
            throw new CodecException(e);
        }
        return buffer;
    }

    @SuppressWarnings("unchecked")
    private void encode0(ByteBuf buffer, Request req, ServiceMeta meta) throws CodecException {
        //[1:serviceLen][1-238:serviceName][4:requestId][4:clientId][4:serverId][1:bodyType][4:bodyLen]
        int servLen = meta.getServiceName().length();
        buffer.writeByte(servLen);
        // service name
        buffer.writeCharSequence(meta.getServiceName(), Constants.UTF8);
        buffer.writeIntLE(req.getReqId());
        // 4clientId|4serverId|1:bodyType
        buffer.writeZero(13);
        Object body = req.getBody();
        if (body != null) {
            int bodyLenIndex = buffer.writerIndex() - 4;
            int bodyLen = meta.getRequestMapper().writeTo(body, buffer, 0);
            int bodyEndIndex = buffer.writerIndex();
            buffer.writerIndex(bodyLenIndex);
            // 小端写bodyLen
            buffer.writeIntLE(bodyLen);
            // 指针重置到末尾
            buffer.writerIndex(bodyEndIndex);
        }
    }


}
