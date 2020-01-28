package com.zhw.skynet.core.protocol;

import io.netty.buffer.ByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ReqAction extends Action<ByteBuf> {

    private ByteBuf buf;

    public ByteBuf fetchReqBuf() {
        ByteBuf buf = this.buf;
        this.buf = null;
        return buf;
    }

    private int reqId;
    private int clientId;
    private int serverId;
    private int status;

    private int bodyLen = -1;

    public ReqAction(ByteBuf buf) {
        this.buf = buf;
    }

    public ReqAction(BiConsumer<ByteBuf, Throwable> consumer, ByteBuf buf) {
        super(consumer);
        this.buf = buf;
    }

    public int getReqId() {
        return reqId;
    }

    public void setReqId(int reqId) {
        this.reqId = reqId;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getBodyLen() {
        return bodyLen;
    }

    public void setBodyLen(int bodyLen) {
        this.bodyLen = bodyLen;
    }
}
