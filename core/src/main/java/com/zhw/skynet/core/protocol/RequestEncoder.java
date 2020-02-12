package com.zhw.skynet.core.protocol;

import com.zhw.skynet.core.Body;
import com.zhw.skynet.core.Request;
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
    public ByteBuf encode(Request req) {
        //|1|4reqId|4clientId|4serverId|1|service|1|method|4bodyLen|body|
        ByteBuf buffer = allocator.buffer(256);
        try {
            encode0(req, buffer);
        } catch (Throwable e) {
            buffer.release();
            throw new EncodeException(e);
        }
        return buffer;
    }

    private void encode0(Request req, ByteBuf buffer) {
        //[1:serviceLen][1-238:serviceName][4:requestId][4:clientId][4:serverId][1:bodyType][4:bodyLen]
        int servLen = req.getService().length();
        buffer.writeByte(servLen);
        // service name
        buffer.writeCharSequence(req.getService(), Codec.UTF8);
        buffer.writeIntLE(req.getReqId());
        // 4clientId|4serverId|1:bodyType
        buffer.writeZero(13);
        Body body = req.getBody();
        if (body != null) {
            int bodyLenIndex = buffer.writerIndex() - 4;
            int bodyLen = body.writeTo(buffer);
            int bodyEndIndex = buffer.writerIndex();
            buffer.writerIndex(bodyLenIndex);
            // 小端写bodyLen
            buffer.writeIntLE(bodyLen);
            // 指针重置到末尾
            buffer.writerIndex(bodyEndIndex);
        }
    }

}
