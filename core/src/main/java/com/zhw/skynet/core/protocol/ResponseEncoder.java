package com.zhw.skynet.core.protocol;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.core.EncodeException;
import com.zhw.skynet.core.Response;
import com.zhw.skynet.core.ServiceMeta;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;

public class ResponseEncoder implements Encoder<Response> {
    private final ByteBufAllocator allocator;

    public ResponseEncoder() {
        this(ByteBufAllocator.DEFAULT);
    }

    public ResponseEncoder(ByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    @Override
    public ByteBuf encode(Response response, ServiceMeta meta) throws EncodeException {
        ByteBuf buffer = allocator.buffer();
        try {
            encode0(buffer, response, meta);
        } catch (Throwable e) {
            buffer.release();
            if (e instanceof EncodeException) {
                throw e;
            }
            throw new EncodeException(e);
        }
        return buffer;
    }

    @SuppressWarnings("unchecked")
    private void encode0(ByteBuf buffer, Response response, ServiceMeta meta) throws EncodeException {
        buffer.writeByte(Constants.RESPONSE_HEAD_LEN);
        /*
         response.setRequestId(in.readIntLE());
         response.setClientId(in.readIntLE());
         response.setServerId(in.readIntLE());
         response.setResponseCode(in.readByte() & 0xFF);
         response.setBodyType(in.readByte() & 0xFF);
         response.setBodyLen(in.readIntLE());
         */
        buffer.writeIntLE(response.getReqId());
        buffer.writeIntLE(response.getClientId());
        buffer.writeIntLE(response.getServerId());
        buffer.writeByte(response.getCode());
        buffer.writeByte(response.getBodyType());
        buffer.writeZero(4);
        int dataLen = 0;
        Object body = response.getBody();
        if (body != null) {
            if (response.getCode() == 0) {
                dataLen = meta.getResponseMapper().writeTo(body, buffer, 0);
            } else {
                dataLen = buffer.writeCharSequence(body.toString(), Constants.UTF8);
            }
        } else {
            if (response.getCode() == 0) {
                // 没有返回body
                return;
            }
            Throwable err = response.getErr();
            while (err != null) {
                buffer.writeByte('[');
                dataLen += buffer.writeCharSequence(err.getClass().getSimpleName(), Constants.UTF8);
                buffer.writeByte(']');
                dataLen += buffer.writeCharSequence(err.getMessage(), Constants.UTF8);
                dataLen += 2;
                err = err.getCause();
            }
        }
        if (dataLen != 0) {
            int end = buffer.writerIndex();
            buffer.writerIndex(end - dataLen - 4);
            buffer.writeIntLE(dataLen);
            buffer.writerIndex(end);
        }
    }
}
