package com.zhw.skynet.core.server;

import com.zhw.skynet.common.RpcException;
import com.zhw.skynet.core.Response;
import com.zhw.skynet.core.ServiceMeta;
import com.zhw.skynet.core.protocol.Codec;
import com.zhw.skynet.core.protocol.RequestMessage;
import io.netty.buffer.ByteBuf;

public class ServerCodec implements Codec<Response, RequestMessage> {
    @Override
    public ByteBuf encode(Response response, ServiceMeta meta) throws RpcException {
        return null;
    }

    @Override
    public RequestMessage decodeRequest(ByteBuf in) throws RpcException {
        return null;
    }

    @Override
    public Object decodeBody(RequestMessage in, ServiceMeta meta) throws RpcException {
        return null;
    }
}
