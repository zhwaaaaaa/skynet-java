package com.zhw.skynet.core.protocol;

import io.netty.buffer.ByteBuf;

public interface Encoder<T> {
    ByteBuf encode(T t) throws EncodeException;
}
