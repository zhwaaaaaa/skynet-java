package com.zhw.skynet.core.sh;

import java.util.List;

public class ShakeHandsResp {
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

    private List<ServiceCount> counts;

    public ShakeHandsResp(List<ServiceCount> counts) {
        this.counts = counts;
    }

    public List<ServiceCount> getCounts() {
        return counts;
    }

    public void setCounts(List<ServiceCount> counts) {
        this.counts = counts;
    }

    public void addServiceCount(String service, int count) {
        counts.add(new ServiceCount(service, count));
    }
}
