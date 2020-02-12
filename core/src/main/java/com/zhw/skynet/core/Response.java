package com.zhw.skynet.core;

public class Response {
    private int code;
    private String errorMsg;
    private int reqId;
    private int clientId;
    private int serverId;
    private int bodyType;
    private int bodyLen;
    private String service;
    private Body body;

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

    public int getBodyLen() {
        return bodyLen;
    }

    public void setBodyLen(int bodyLen) {
        this.bodyLen = bodyLen;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getBodyType() {
        return bodyType;
    }

    public void setBodyType(int bodyType) {
        this.bodyType = bodyType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Response{");
        sb.append("code=").append(code);
        sb.append(", errorMsg='").append(errorMsg).append('\'');
        sb.append(", reqId=").append(reqId);
        sb.append(", clientId=").append(clientId);
        sb.append(", serverId=").append(serverId);
        sb.append(", bodyType=").append(bodyType);
        sb.append(", bodyLen=").append(bodyLen);
        sb.append(", service='").append(service).append('\'');
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}
