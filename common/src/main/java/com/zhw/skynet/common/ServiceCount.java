package com.zhw.skynet.common;

public class ServiceCount {
    private String serviceName;
    private int count;

    public ServiceCount() {
    }

    public ServiceCount(String serviceName) {
        this.serviceName = serviceName;
    }

    public ServiceCount(String serviceName, int count) {
        this.serviceName = serviceName;
        this.count = count;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
