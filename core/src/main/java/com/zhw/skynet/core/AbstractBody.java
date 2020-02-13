package com.zhw.skynet.core;

import io.netty.util.ReferenceCounted;

public abstract class AbstractBody implements Body {

    @Override
    public int refCnt() {
        return 0;
    }

    @Override
    public ReferenceCounted retain() {
        return this;
    }

    @Override
    public ReferenceCounted retain(int increment) {
        return this;
    }

    @Override
    public ReferenceCounted touch() {
        return this;
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        return this;
    }

    @Override
    public boolean release() {
        return false;
    }

    @Override
    public boolean release(int decrement) {
        return false;
    }
}
