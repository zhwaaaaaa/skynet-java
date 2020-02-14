package com.zhw.skynet.core.protocol;

import com.zhw.skynet.core.Action;
import com.zhw.skynet.core.ServiceMeta;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ShakeHandsReq extends Action<List<ShakeHandsReq.ServiceCount>> {
    public static class ServiceCount {
        private String name;
        private int providerCount;

        public ServiceCount(String name, int providerCount) {
            this.name = name;
            this.providerCount = providerCount;
        }

        public String getName() {
            return name;
        }

        public int getProviderCount() {
            return providerCount;
        }

        @Override
        public String toString() {
            return "Service Count{" + name + ":" + providerCount + '}';
        }
    }

    private Collection<ServiceMeta> services;

    public ShakeHandsReq(Collection<ServiceMeta> services) {
        this.services = services;
    }

    public Collection<ServiceMeta> getServices() {
        return services;
    }
}
