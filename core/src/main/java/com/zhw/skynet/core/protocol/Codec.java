package com.zhw.skynet.core.protocol;

import com.zhw.skynet.common.RpcException;
import com.zhw.skynet.core.ServiceMeta;
import io.netty.buffer.ByteBuf;

public interface Codec<E, D> {
    ByteBuf encode(E e, ServiceMeta meta) throws RpcException;

    D decodeRequest(ByteBuf in) throws RpcException;

    Object decodeBody(D in, ServiceMeta meta) throws RpcException;
}
