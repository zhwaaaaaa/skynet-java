package com.zhw.skynet.core.protocol;

import io.netty.buffer.ByteBuf;

public class CompositeCodec<E, D> implements Codec<E, D> {
    private final Encoder<E> encoder;
    private final Decoder<D> decoder;

    public CompositeCodec(Encoder<E> encoder, Decoder<D> decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public D decode(ByteBuf buf) throws DecodeException {
        return decoder.decode(buf);
    }

    @Override
    public ByteBuf encode(E e) throws EncodeException {
        return encoder.encode(e);
    }
}
