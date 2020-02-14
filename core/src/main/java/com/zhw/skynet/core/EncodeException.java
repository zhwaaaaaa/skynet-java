package com.zhw.skynet.core;

import com.zhw.skynet.common.RpcException;

public class EncodeException extends RpcException {
    public EncodeException() {
    }

    public EncodeException(String message) {
        super(message);
    }

    public EncodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncodeException(Throwable cause) {
        super(cause);
    }
}
