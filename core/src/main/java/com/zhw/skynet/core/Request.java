package com.zhw.skynet.core;

import java.util.concurrent.atomic.AtomicInteger;

public class Request {
    private static final AtomicInteger _ID = new AtomicInteger();
    private final int reqId;
    private ServiceMeta meta;
    private Object body;

    public Request(ServiceMeta meta, Object body) {
        reqId = _ID.incrementAndGet();
        this.meta = meta;
        this.body = body;
    }

    public Request(int reqId, ServiceMeta meta, Object body) {
        this.reqId = reqId;
        this.meta = meta;
        this.body = body;
    }

    public int getReqId() {
        return reqId;
    }


    public ServiceMeta getMeta() {
        return meta;
    }

    public void setMeta(ServiceMeta meta) {
        this.meta = meta;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
