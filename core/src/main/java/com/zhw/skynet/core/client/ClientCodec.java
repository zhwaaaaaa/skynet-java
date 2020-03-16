package com.zhw.skynet.core.client;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.common.RpcException;
import com.zhw.skynet.core.MsgType;
import com.zhw.skynet.core.Request;
import com.zhw.skynet.core.ServiceMeta;
import com.zhw.skynet.core.protocol.Codec;
import com.zhw.skynet.core.protocol.CodecException;
import com.zhw.skynet.core.protocol.ResponseMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class ClientCodec implements Codec<Request, ResponseMessage> {
    private final ByteBufAllocator allocator;

    public ClientCodec(ByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    @Override
    public ByteBuf encode(Request request, ServiceMeta meta) throws RpcException {
        ByteBuf buf = allocator.buffer();
        try {
            encode0(buf, request, meta);
        } catch (Throwable e) {
            buf.release();
            if (e instanceof RpcException) {
                throw (RpcException) e;
            } else {
                throw new CodecException("encode error", e);
            }
        }
        return buf;
    }

    private void encode0(ByteBuf buf, Request request, ServiceMeta meta) throws Throwable {
        buf.writeByte(MsgType.MT_REQUEST.val());
        buf.writeZero(4);
        buf.writeIntLE(request.getReqId());
        buf.writeZero(8);
        buf.writeByte(meta.getServiceName().length());
        buf.writeCharSequence(meta.getServiceName(), Constants.UTF8);
        buf.writeByte(meta.getMethod().length());
        buf.writeCharSequence(meta.getMethod(), Constants.UTF8);
        buf.writeByte(0);
        meta.getRequestMapper().writeTo(request.getBody(), buf, 0);
        buf.setIntLE(1, buf.readableBytes() - 5);
    }

    @Override
    public ResponseMessage decodeRequest(ByteBuf in) throws RpcException {
        MsgType type = MsgType.from(in.readByte() & 0xFF);
        if (type != MsgType.MT_RESPONSE) {
            throw new CodecException("invalid message type " + type);
        }
        ResponseMessage msg = new ResponseMessage();
        msg.setMsgLen(in.readIntLE());
        msg.setRequestId(in.readIntLE());
        msg.setClientId(in.readIntLE());
        msg.setServerId(in.readIntLE());
        msg.setResponseCode(in.readByte() & 0xFF);
        msg.setBodyType(in.readByte() & 0xFF);
        msg.setBodyBuf(in);
        return msg;
    }

    @Override
    public Object decodeBody(ResponseMessage in, ServiceMeta meta) throws RpcException {
        int code = in.getResponseCode();
        ByteBuf byteBuf = in.getBodyBuf();
        int len = byteBuf.readableBytes();
        if (code != 0 && len > 0) {
            return byteBuf.readCharSequence(len, Constants.UTF8);
        }
        return meta.getResponseMapper().read(byteBuf, in.getBodyType());
    }
}
