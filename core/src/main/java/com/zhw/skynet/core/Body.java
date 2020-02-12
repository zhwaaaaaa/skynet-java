package com.zhw.skynet.core;

import com.zhw.skynet.common.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

public interface Body extends ReferenceCounted {

    int writeTo(ByteBuf buf);

    <T> T read(TypeReference<T> type);
}
