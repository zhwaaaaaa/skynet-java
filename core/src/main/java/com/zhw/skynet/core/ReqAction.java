package com.zhw.skynet.core;

import com.zhw.skynet.core.protocol.Action;

import java.util.function.BiConsumer;


public class ReqAction extends Action<Response> {

    private Request request;

    public ReqAction(Request request) {
        this.request = request;
    }

    public ReqAction(BiConsumer<Response, Throwable> consumer,
                     Request request) {
        super(consumer);
        this.request = request;
    }
}
