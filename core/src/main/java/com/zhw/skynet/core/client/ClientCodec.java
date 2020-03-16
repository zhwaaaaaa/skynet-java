package com.zhw.skynet.core.client;

import com.zhw.skynet.common.RpcException;
import com.zhw.skynet.core.Request;
import com.zhw.skynet.core.ServiceMeta;
import com.zhw.skynet.core.protocol.Codec;
import com.zhw.skynet.core.protocol.ResponseMessage;
import io.netty.buffer.ByteBuf;

public class ClientCodec implements Codec<Request, ResponseMessage> {
    @Override
    public ByteBuf encode(Request request, ServiceMeta meta) throws RpcException {
        return null;
    }

    @Override
    public ResponseMessage decodeRequest(ByteBuf in) throws RpcException {
        return null;
    }

    @Override
    public Object decodeBody(ResponseMessage in, ServiceMeta meta) throws RpcException {
        return null;
    }
}
