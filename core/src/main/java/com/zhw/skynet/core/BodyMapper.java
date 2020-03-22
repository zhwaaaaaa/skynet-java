package com.zhw.skynet.core;

import com.zhw.skynet.core.protocol.CodecException;
import io.netty.buffer.ByteBuf;

public interface BodyMapper {

    String typeDesc();

    void writeTo(Object data, ByteBuf out, int writeOpts) throws CodecException;

    Object read(ByteBuf in, int readOpts) throws CodecException;
}
