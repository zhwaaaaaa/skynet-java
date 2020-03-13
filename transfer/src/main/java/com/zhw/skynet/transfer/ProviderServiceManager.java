package com.zhw.skynet.transfer;

public class ProviderServiceManager implements ServiceManager {


    @Override
    public int addChannelService(ServiceDesc desc, int channelId) {
        return 0;
    }

    @Override
    public int removeChannelService(ServiceDesc desc, int channelId) {
        return 0;
    }

    @Override
    public RemoteEndPointGroup get(String service) {
        return null;
    }

    @Override
    public void putResponseReceiver(int channelId, RemoteEndPoint remoteEndPoint) {

    }

    @Override
    public RemoteEndPoint getResponseReceiver(int channelId) {
        return null;
    }

    @Override
    public RemoteEndPoint removeResponseReceiver(int channelId) {
        return null;
    }

    @Override
    public void close() {

    }
}
