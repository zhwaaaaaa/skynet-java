package com.zhw.skynet.core.protocol;

import com.zhw.skynet.common.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;

import java.util.List;

public class ResponseMessageHandler extends ByteToMessageDecoder {
    private ResponseMessage response;
    public static final int RESPONSE_HEAD_PKG_LEN = Constants.RESPONSE_HEAD_LEN + 1;

    public ResponseMessageHandler() {
        setCumulator(COMPOSITE_CUMULATOR);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int bodyLen;
        if (response == null) {
            if (in.readableBytes() < RESPONSE_HEAD_PKG_LEN) {
                return;
            }
            int headerLen = in.readByte() & 0xFF;
            if (headerLen != Constants.RESPONSE_HEAD_LEN) {
                ctx.channel().close();
                throw new DecoderException("response header len expect 18,get " + headerLen);
            }
            response = new ResponseMessage();
            response.setRequestId(in.readIntLE());
            response.setClientId(in.readIntLE());
            response.setServerId(in.readIntLE());
            response.setResponseCode(in.readByte() & 0xFF);
            response.setBodyType(in.readByte() & 0xFF);
            bodyLen = in.readIntLE();
            if (bodyLen == 0) {
                out.add(response);
                response = null;
                return;
            }
            response.setBodyLen(bodyLen);
        } else {
            bodyLen = response.getBodyLen();
        }
        if (in.readableBytes() >= bodyLen) {
            ByteBuf body = in.retainedSlice(in.readerIndex(), bodyLen);
            response.setBodyBuf(body);
            in.skipBytes(bodyLen);
            out.add(response);
            response = null;
        }
    }
}
