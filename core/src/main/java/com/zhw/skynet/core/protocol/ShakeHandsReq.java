package com.zhw.skynet.core.protocol;

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

    private Set<String> services;

    public ShakeHandsReq(Set<String> services) {
        this.services = services;
    }

    public Set<String> getServices() {
        return services;
    }
}
