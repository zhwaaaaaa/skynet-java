package com.zhw.skynet.core.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

public class Message implements ReferenceCounted {
    private int requestId;
    private int clientId;
    private int serverId;
    private int bodyType;
    private int bodyLen;
    private ByteBuf bodyBuf;

    public ByteBuf getBodyBuf() {
        return bodyBuf;
    }

    public void setBodyBuf(ByteBuf bodyBuf) {
        this.bodyBuf = bodyBuf;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getBodyLen() {
        return bodyLen;
    }

    public void setBodyLen(int bodyLen) {
        this.bodyLen = bodyLen;
    }

    public int getBodyType() {
        return bodyType;
    }

    public void setBodyType(int bodyType) {
        this.bodyType = bodyType;
    }


    @Override
    public int refCnt() {
        if (bodyBuf != null) {
            return bodyBuf.refCnt();
        }
        return 0;
    }

    @Override
    public ReferenceCounted retain() {
        if (bodyBuf != null) {
            return bodyBuf.retain();
        }
        return null;
    }

    @Override
    public ReferenceCounted retain(int increment) {
        if (bodyBuf != null) {
            return bodyBuf.retain(increment);
        }
        return null;
    }

    @Override
    public ReferenceCounted touch() {
        if (bodyBuf != null) {
            return bodyBuf.touch();
        }
        return null;
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        if (bodyBuf != null) {
            return bodyBuf.touch(hint);
        }
        return null;
    }

    @Override
    public boolean release() {
        if (bodyBuf != null) {
            return bodyBuf.release();
        }
        return true;
    }

    @Override
    public boolean release(int decrement) {
        if (bodyBuf != null) {
            return bodyBuf.release();
        }
        return true;
    }
}
