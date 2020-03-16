package com.zhw.skynet.core.server;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.core.ServiceMeta;
import com.zhw.skynet.core.protocol.RequestMessage;

import java.util.Objects;

public class ServiceMetaKey {

    public static ServiceMetaKey of(ServiceMeta meta) {
        return new ServiceMetaKey(meta.getServiceName(), meta.getMethod());
    }

    public static ServiceMetaKey of(RequestMessage meta) {
        return new ServiceMetaKey(meta.getService(), meta.getMethod());
    }

    private final String service;
    private final String method;

    ServiceMetaKey(String service, String method) {
        this.service = service;
        this.method = method;
    }

    public String getService() {
        return service;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceMetaKey that = (ServiceMetaKey) o;
        return Objects.equals(service, that.service) &&
                Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, method);
    }

    @Override
    public String toString() {
        return service + Constants.SERVICE_NAME_SEPARATOR + method;
    }
}
