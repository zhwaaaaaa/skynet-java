package com.zhw.skynet.core.protocol;

public class RequestMessage extends Message {
    private int serviceLen;
    private String service;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public RequestMessage(int serviceLen) {
        this.serviceLen = serviceLen;
    }

    public int getServiceLen() {
        return serviceLen;
    }

    public void setServiceLen(int serviceLen) {
        this.serviceLen = serviceLen;
    }
}
