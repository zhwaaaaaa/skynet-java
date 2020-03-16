package com.zhw.skynet.core.sh;

import com.zhw.skynet.common.RpcException;

public class ShakeHandsException extends RpcException {
    public ShakeHandsException() {
    }

    public ShakeHandsException(String message) {
        super(message);
    }
}
