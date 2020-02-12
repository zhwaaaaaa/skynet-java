package com.zhw.skynet.core.protocol;

import com.zhw.skynet.core.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;

import java.util.List;

public class ProtocolHandler extends ByteToMessageDecoder {
    static final String HANDLER_NAME = "Codec";

    private Response response;

    public ProtocolHandler() {
        setCumulator(COMPOSITE_CUMULATOR);
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (response == null) {
            if (in.readableBytes() < 19) {
                return;
            }
            int headerLen = in.readByte() & 0xFF;
            if (headerLen != 18) {
                ctx.channel().close();
                throw new DecoderException("response header len expect 18,get " + headerLen);
            }
            response = new Response();
            response.setReqId(in.readIntLE());
            response.setClientId(in.readIntLE());
            response.setServerId(in.readIntLE());
            response.setCode(in.readByte() & 0xFF);
            response.setBodyType(in.readByte() & 0xFF);
            response.setBodyLen(in.readIntLE());
        }
        if (in.readableBytes() >= response.getBodyLen()) {
            ByteBuf body = in.retainedSlice(in.readerIndex(), response.getBodyLen());
            response.setBody(new ByteBufBody(body));
            in.skipBytes(response.getBodyLen());
            out.add(response);
            response = null;
        }
    }
}
