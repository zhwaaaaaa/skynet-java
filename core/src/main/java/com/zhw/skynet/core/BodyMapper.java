package com.zhw.skynet.core;

import io.netty.buffer.ByteBuf;

public interface BodyMapper<T> {

    String typeDesc();

    int writeTo(T data, ByteBuf out, int writeOpts) throws EncodeException;

    T read(ByteBuf in, int readOpts) throws DecodeException;
}
