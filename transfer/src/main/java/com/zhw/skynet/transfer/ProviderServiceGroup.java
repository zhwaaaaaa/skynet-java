package com.zhw.skynet.transfer;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class ProviderServiceGroup implements RemoteEndPointGroup {
    private List<RemoteEndPoint> endPoints = new ArrayList<>(4);
    private final String serviceName;
    private int index;

    public ProviderServiceGroup(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean send(ByteBuf msg) {
    }

    @Override
    public int subscribeSize() {
        return endPoints.size();
    }

    @Override
    public void close() {

    }
}
