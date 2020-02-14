package com.zhw.skynet.core;

import com.zhw.skynet.core.protocol.ResponseMessage;

import java.util.function.BiConsumer;


public class ReqAction extends Action<ResponseMessage> {

    private Request request;

    public ReqAction(Request request) {
        this.request = request;
    }

    public ReqAction(Request request, BiConsumer<ResponseMessage, Throwable> consumer) {
        super(consumer);
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }
}
