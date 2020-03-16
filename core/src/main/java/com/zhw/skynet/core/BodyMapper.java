package com.zhw.skynet.core;

import com.zhw.skynet.core.protocol.CodecException;
import io.netty.buffer.ByteBuf;

public interface BodyMapper<T> {

    String typeDesc();

    int writeTo(T data, ByteBuf out, int writeOpts) throws CodecException;

    T read(ByteBuf in, int readOpts) throws DecodeException;
}
