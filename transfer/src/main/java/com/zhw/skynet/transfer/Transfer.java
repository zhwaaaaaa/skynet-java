package com.zhw.skynet.transfer;

import io.netty.channel.EventLoopGroup;

public interface Transfer {
    void init(EventLoopGroup group) throws Exception;

    void close() throws Exception;
}
