package com.zhw.skynet.core.protocol;

import io.netty.buffer.ByteBuf;

public interface Decoder<T> {
    T decode(ByteBuf buf) throws DecodeException;
}
