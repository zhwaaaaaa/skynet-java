package com.zhw.skynet.transfer;

import java.util.Objects;

public class ServiceDesc {
    private String serviceName;
    private int version;
    private String param;
    private String result;

    public ServiceDesc(String serviceName, int version, String param, String result) {
        this.serviceName = serviceName;
        this.version = version;
        this.param = param;
        this.result = result;
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

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceDesc that = (ServiceDesc) o;
        return version == that.version &&
                Objects.equals(serviceName, that.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, version);
    }
}
