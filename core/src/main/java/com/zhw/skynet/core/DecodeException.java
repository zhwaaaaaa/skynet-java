package com.zhw.skynet.core;

import com.zhw.skynet.common.RpcException;

public class DecodeException extends RpcException {
    public DecodeException() {
    }

    public DecodeException(String message) {
        super(message);
    }

    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodeException(Throwable cause) {
        super(cause);
    }
}
