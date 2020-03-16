package com.zhw.skynet.core.server;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.common.RpcException;
import com.zhw.skynet.core.MsgType;
import com.zhw.skynet.core.Response;
import com.zhw.skynet.core.ServiceMeta;
import com.zhw.skynet.core.protocol.Codec;
import com.zhw.skynet.core.protocol.CodecException;
import com.zhw.skynet.core.protocol.RequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class ServerCodec implements Codec<Response, RequestMessage> {
    private final ByteBufAllocator allocator;

    public ServerCodec(ByteBufAllocator allocator) {
        this.allocator = allocator;
    }

    @Override
    public ByteBuf encode(Response response, ServiceMeta meta) throws RpcException {
        ByteBuf buf = allocator.buffer();
        try {
            encode0(buf, response, meta);
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

    private void encode0(ByteBuf buf, Response response, ServiceMeta meta) throws Throwable {
        buf.writeByte(MsgType.MT_RESPONSE.val());
        buf.writeZero(4);
        buf.writeIntLE(response.getReqId());
        buf.writeIntLE(response.getClientId());
        buf.writeIntLE(response.getServerId());
        buf.writeByte(response.getCode());
        buf.writeByte(response.getBodyType());
        if (response.getCode() != 0) {
            assert response.getErr() != null;
            writeErrorMsg(buf, response.getErr());
        } else {
            meta.getResponseMapper().writeTo(response.getBody(), buf, 0);
        }
        buf.setIntLE(1, buf.readableBytes() - 5);
    }

    private void writeErrorMsg(ByteBuf sb, Throwable err) {
        sb.writeByte('[');
        sb.writeCharSequence(err.getClass().getSimpleName(), Constants.UTF8);
        sb.writeByte(']');
        sb.writeCharSequence(err.getMessage(), Constants.UTF8);
        err = err.getCause();
        while (err != null) {
            sb.writeCharSequence("->", Constants.UTF8);
            sb.writeByte('[');
            sb.writeCharSequence(err.getClass().getSimpleName(), Constants.UTF8);
            sb.writeByte(']');
            sb.writeCharSequence(err.getMessage(), Constants.UTF8);
            err = err.getCause();
        }
    }

    @Override
    public RequestMessage decodeRequest(ByteBuf in) throws RpcException {
        MsgType type = MsgType.from(in.readByte() & 0xFF);
        if (type != MsgType.MT_REQUEST) {
            throw new RpcException("invalid message type " + type);
        }
        RequestMessage msg = new RequestMessage();
        msg.setMsgLen(in.readIntLE());
        msg.setRequestId(in.readIntLE());
        msg.setClientId(in.readIntLE());
        msg.setServerId(in.readIntLE());
        int servLen = in.readByte() & 0xFF;
        if (in.readableBytes() <= servLen) {
            throw new CodecException("invalid request to long service");
        }
        msg.setService(in.readCharSequence(servLen, Constants.UTF8).toString());
        int methodLen = in.readByte() & 0xFF;
        if (in.readableBytes() <= methodLen) {
            throw new CodecException("invalid request to long method");
        }
        msg.setMethod(in.readCharSequence(methodLen, Constants.UTF8).toString());
        msg.setBodyType(in.readByte() & 0xFF);
        msg.setBodyBuf(in);
        return msg;
    }

    @Override
    public Object decodeBody(RequestMessage msg, ServiceMeta meta) throws RpcException {
        return meta.getRequestMapper().read(msg.getBodyBuf(), msg.getBodyType());
    }
}
