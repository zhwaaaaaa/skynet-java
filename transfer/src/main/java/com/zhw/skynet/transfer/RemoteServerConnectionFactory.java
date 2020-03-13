package com.zhw.skynet.transfer;

import com.zhw.skynet.transfer.discovery.ServiceInstance;

public interface RemoteServerConnectionFactory {

    RemoteServerConnection getOrCreateConn(String ip, int port);

}
