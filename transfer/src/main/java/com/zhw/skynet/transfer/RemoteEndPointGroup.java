package com.zhw.skynet.transfer;

import io.netty.buffer.ByteBuf;

public interface RemoteEndPointGroup {

    String getServiceName();

    boolean send(ByteBuf msg);

    int subscribeSize();

    void close();

}
