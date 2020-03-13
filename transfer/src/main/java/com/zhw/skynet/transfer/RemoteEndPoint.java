package com.zhw.skynet.transfer;

import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Set;

public interface RemoteEndPoint {

    boolean send(ByteBuf buf);

    Set<String> relateServices();

    void close();


}
