package com.zhw.skynet.core.protocol;

import com.zhw.skynet.common.TypeReference;
import com.zhw.skynet.core.Body;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

public class ByteBufBody implements Body {
    private ByteBuf buf;

    public ByteBufBody(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public int writeTo(ByteBuf buf) {
        int bytes = this.buf.readableBytes();
        buf.writeBytes(this.buf);
        return bytes;
    }

    @Override
    public <T> T read(TypeReference<T> type) {
        return null;
    }

    @Override
    public int refCnt() {
        return buf.refCnt();
    }

    @Override
    public ReferenceCounted retain() {
        return buf.retain();
    }

    @Override
    public ReferenceCounted retain(int increment) {
        return buf.retain();
    }

    @Override
    public ReferenceCounted touch() {
        return buf.touch();
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        return buf.touch(hint);
    }

    @Override
    public boolean release() {
        return buf.release();
    }

    @Override
    public boolean release(int decrement) {
        return buf.release(decrement);
    }
}
