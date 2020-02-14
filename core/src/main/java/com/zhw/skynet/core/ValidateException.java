package com.zhw.skynet.core;

import com.zhw.skynet.common.RpcException;

public class ValidateException extends RpcException {
    public ValidateException() {
    }

    public ValidateException(String message) {
        super(message);
    }

    public ValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidateException(Throwable cause) {
        super(cause);
    }
}
