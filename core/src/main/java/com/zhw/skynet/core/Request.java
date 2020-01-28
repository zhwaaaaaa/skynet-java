package com.zhw.skynet.core;

import com.zhw.skynet.common.TypeReference;
import io.netty.util.internal.StringUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class Request {
    public static final int MAX_SERVICE_LEN = 157;
    public static final int MAX_METHOD_LEN = 80;
    private static final AtomicInteger ID = new AtomicInteger();


    private final int reqId = ID.getAndIncrement();
    private long timeout = 10000L;
    private String service;
    private String method;
    private Body body;
    private TypeReference<?> respBodyType;

    public Request(String service, String method, Body body) {
        this.service = service;
        this.method = method;
        this.body = body;
    }

    public Request(String service, String method, Body body, long timeout) {
        this.service = service;
        this.method = method;
        this.body = body;
        this.timeout = timeout;
    }

    public int getReqId() {
        return reqId;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        if (StringUtil.isNullOrEmpty(service) || service.length() > MAX_SERVICE_LEN) {
            throw new IllegalArgumentException("service length 1-" + MAX_SERVICE_LEN);
        }
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        if (StringUtil.isNullOrEmpty(method) || service.length() > MAX_METHOD_LEN) {
            throw new IllegalArgumentException("service length 1-" + MAX_METHOD_LEN);
        }
        this.method = method;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public TypeReference<?> getRespBodyType() {
        return respBodyType;
    }

    public void setRespBodyType(TypeReference<?> respBodyType) {
        this.respBodyType = respBodyType;
    }
}
