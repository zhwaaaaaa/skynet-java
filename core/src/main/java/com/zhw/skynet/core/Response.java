package com.zhw.skynet.core;

public class Response {
    private int code;
    private int reqId;
    private int clientId;
    private int serverId;
    private int bodyType;
    private Object body;
    private Throwable err;

    public Response() {
    }

    public Response(int code, int reqId, int clientId, int serverId, int bodyType, Object body) {
        this.code = code;
        this.reqId = reqId;
        this.clientId = clientId;
        this.serverId = serverId;
        this.bodyType = bodyType;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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

    public int getBodyType() {
        return bodyType;
    }

    public void setBodyType(int bodyType) {
        this.bodyType = bodyType;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Throwable getErr() {
        return err;
    }

    public void setErr(Throwable err) {
        this.err = err;
    }

    @Override
    public String toString() {
        return "Response{" + "code=" + code +
                ", reqId=" + reqId +
                ", clientId=" + clientId +
                ", serverId=" + serverId +
                ", bodyType=" + bodyType +
                ", body=" + body +
                '}';
    }
}
