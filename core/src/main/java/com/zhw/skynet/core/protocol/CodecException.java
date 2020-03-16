package com.zhw.skynet.core.protocol;

import com.zhw.skynet.common.RpcException;

public class CodecException extends RpcException {
    public CodecException() {
    }

    public CodecException(String message) {
        super(message);
    }

    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodecException(Throwable cause) {
        super(cause);
    }
}
