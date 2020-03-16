package com.zhw.skynet.core;

import com.zhw.skynet.common.Constants;

public class ServiceMeta {
    private String serviceName;
    private String method;
    private int version;
    private long timeoutMs = 10000;
    private BodyMapper requestMapper;
    private BodyMapper responseMapper;

    public ServiceMeta() {
    }

    public ServiceMeta(String serviceName) {
        setServiceName(serviceName);
    }

    public ServiceMeta(String serviceName, BodyMapper requestMapper, BodyMapper responseMapper) {
        this.serviceName = serviceName;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        if (serviceName.length() > Constants.MAX_SERVICE_LEN) {
            throw new IllegalArgumentException("MAX_SERVICE_LEN 238, get" + serviceName.length());
        }
        this.serviceName = serviceName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public BodyMapper getRequestMapper() {
        return requestMapper;
    }

    public void setRequestMapper(BodyMapper requestMapper) {
        this.requestMapper = requestMapper;
    }

    public BodyMapper getResponseMapper() {
        return responseMapper;
    }

    public void setResponseMapper(BodyMapper responseMapper) {
        this.responseMapper = responseMapper;
    }
}
