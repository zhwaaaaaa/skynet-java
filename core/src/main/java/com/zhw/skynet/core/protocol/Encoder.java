package com.zhw.skynet.core.protocol;

import com.zhw.skynet.core.EncodeException;
import com.zhw.skynet.core.ServiceMeta;
import io.netty.buffer.ByteBuf;

public interface Encoder<T> {

    ByteBuf encode(T t, ServiceMeta meta) throws EncodeException;

}
