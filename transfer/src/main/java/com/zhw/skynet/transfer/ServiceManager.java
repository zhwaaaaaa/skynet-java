package com.zhw.skynet.transfer;

public interface ServiceManager {

    int addChannelService(ServiceDesc desc, int channelId);

    int removeChannelService(ServiceDesc desc, int channelId);

    RemoteEndPointGroup get(String service);

    void putResponseReceiver(int channelId, RemoteEndPoint remoteEndPoint);

    RemoteEndPoint getResponseReceiver(int channelId);

    RemoteEndPoint removeResponseReceiver(int channelId);

    void close();
}
