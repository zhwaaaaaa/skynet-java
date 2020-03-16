package com.zhw.skynet.common;

import java.nio.charset.Charset;

public class Constants {
    public static final String SERVICE_NAME_SEPARATOR = "/";
    public static final int MAX_SERVICE_LEN = 238;
    public static final int MAX_SERVICE_SIZE = 0xFFFF;
    public static final int RESPONSE_HEAD_LEN = 18;

    public static final Charset UTF8 = Charset.forName("utf-8");

    public static final int CODE_LOCAL_ERROR = 130;// 本地异常
    //服务器报错 40 - 60
    // 服务器收到不正确的消息 40 - 49
    public static final int CODE_SERVER_DECODE_ERROR = 40;
    public static final int CODE_SERVER_PARAM_VALID_ERROR = 41;
    public static final int CODE_SERVER_NO_SERVICE = 42;
    // 服务器运行时错误
    // 同步
    public static final int CODE_SERVER_RUNTIME_ERROR = 50;
    // 异步
    public static final int CODE_SERVER_ASYNC_RUNTIME_ERROR = 51;
    public static final int MAX_MSG_LEN = 1 << 30;//1G

    private Constants() {
    }
}
