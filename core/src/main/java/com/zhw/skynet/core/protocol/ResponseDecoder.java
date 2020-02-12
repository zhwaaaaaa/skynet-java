package com.zhw.skynet.core.protocol;

import com.zhw.skynet.core.Response;
import io.netty.buffer.ByteBuf;

public class ResponseDecoder implements Decoder<Response> {

    /**
     * ResponseId {
     * uint8_t headerLen;
     * uint32_t requestId;
     * uint32_t clientId;
     * uint32_t serverId;
     * uint8_t responseCode;
     * uint8_t bodyType;
     * uint32_t bodyLen;
     * char data[];
     * }
     *
     * @param buf
     * @return
     * @throws DecodeException
     */
    @Override
    public Response decode(ByteBuf buf) throws DecodeException {
        Response response = new Response();
        response.setReqId(buf.readIntLE());
        response.setClientId(buf.readIntLE());
        response.setServerId(buf.readIntLE());
        response.setCode(buf.readByte() & 0xFF);
        response.setBodyType(buf.readByte() & 0xFF);
        response.setBodyLen(buf.readIntLE());
        return response;
    }
}
