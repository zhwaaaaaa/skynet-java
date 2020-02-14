package com.zhw.skynet.core;

public interface ServiceInvoker {
    ServiceMeta getServiceMeta();

    Object invoke(Object param) throws Throwable;
}
