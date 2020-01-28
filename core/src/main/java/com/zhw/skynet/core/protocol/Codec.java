package com.zhw.skynet.core.protocol;

import java.nio.charset.Charset;

public interface Codec<E, D> extends Encoder<E>, Decoder<D> {
    Charset UTF8 = Charset.forName("utf-8");
}
