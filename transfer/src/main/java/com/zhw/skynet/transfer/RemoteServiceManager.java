package com.zhw.skynet.transfer;

import com.zhw.skynet.transfer.discovery.NamingService;

import java.util.HashMap;
import java.util.Map;

public class RemoteServiceManager implements ServiceManager {

    private NamingService namingService;
    private Map<String, ConsumerChannelGroup> groupMap = new HashMap<>();
    private RemoteServerConnectionFactory connFactory;
    private Map<Integer, RemoteEndPoint> respChMap = new HashMap<>();

    public RemoteServiceManager(NamingService nameService) {
        this.namingService = nameService;
    }

    @Override
    public int addChannelService(ServiceDesc desc, int channelId) {
        String serviceName = desc.getServiceName();
        ConsumerChannelGroup group = groupMap.get(serviceName);
        if (group == null) {
            group = new ConsumerChannelGroup(namingService, serviceName, connFactory);
        }
        group.addCareId(channelId);
        return group.subscribeSize();
    }

    @Override
    public int removeChannelService(ServiceDesc desc, int channelId) {
        String serviceName = desc.getServiceName();
        ConsumerChannelGroup group = groupMap.get(serviceName);
        group.removeCareId(channelId);
        int i = group.subscribeSize();
        if (i == 0) {
            groupMap.remove(serviceName);
            group.close();
        }
        return i;
    }

    @Override
    public RemoteEndPointGroup get(String service) {
        return groupMap.get(service);
    }

    @Override
    public void putResponseReceiver(int channelId, RemoteEndPoint remoteEndPoint) {
        RemoteEndPoint old = respChMap.put(channelId, remoteEndPoint);
        assert old == null;
    }

    @Override
    public RemoteEndPoint getResponseReceiver(int channelId) {
        return respChMap.get(channelId);
    }

    @Override
    public RemoteEndPoint removeResponseReceiver(int channelId) {
        return respChMap.remove(channelId);
    }

    @Override
    public void close() {
        for (ConsumerChannelGroup value : groupMap.values()) {
            value.close();
        }
    }


}
