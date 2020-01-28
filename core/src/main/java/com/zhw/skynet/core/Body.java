package com.zhw.skynet.core;

import com.zhw.skynet.common.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

public interface Body<T> extends ReferenceCounted {

    int writeTo(ByteBuf buf);

    T read();

    TypeReference<T> getType();
}
