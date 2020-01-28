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
        int servLen = req.getService().length();
        int methodLen = req.getMethod().length();
        int headLen = servLen + methodLen + 18;
        buffer.writeByte(headLen);
        // 4reqId|4clientId|4serverId|
        buffer.writeZero(12);
        // service name
        buffer.writeByte(servLen);
        buffer.writeCharSequence(req.getService(), Codec.UTF8);
        // method name
        buffer.writeByte(methodLen);
        buffer.writeCharSequence(req.getMethod(), Codec.UTF8);
        // 4bodyLen
        buffer.writeZero(4);
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
