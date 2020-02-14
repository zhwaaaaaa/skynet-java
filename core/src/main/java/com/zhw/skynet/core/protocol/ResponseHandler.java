package com.zhw.skynet.core.protocol;

import com.zhw.skynet.common.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;

import java.util.List;

public class ResponseHandler extends ByteToMessageDecoder {
    private ResponseMessage response;

    public ResponseHandler() {
        setCumulator(COMPOSITE_CUMULATOR);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (response == null) {
            if (in.readableBytes() < 19) {
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
            response.setBodyLen(in.readIntLE());
        }
        if (in.readableBytes() >= response.getBodyLen()) {
            ByteBuf body = in.retainedSlice(in.readerIndex(), response.getBodyLen());
            response.setBodyBuf(body);
            in.skipBytes(response.getBodyLen());
            out.add(response);
            response = null;
        }
    }
}
