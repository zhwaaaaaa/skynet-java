package com.zhw.skynet.core.protocol;

import io.netty.buffer.ByteBuf;

public class Message {
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

    public void releaseBodyBuf() {
        if (bodyBuf != null) {
            bodyBuf.release();
        }
    }
}
