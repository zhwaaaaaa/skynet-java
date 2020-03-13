package com.zhw.skynet.transfer.discovery;

public class ServiceInstance {
    private String ip;
    private int port;
    private String serviceName;
    private int version;

    public ServiceInstance() {
    }

    public ServiceInstance(String ip, int port, String serviceName) {
        this.ip = ip;
        this.port = port;
        this.serviceName = serviceName;
    }

    public ServiceInstance(String ip, int port, String serviceName, int version) {
        this.ip = ip;
        this.port = port;
        this.serviceName = serviceName;
        this.version = version;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
