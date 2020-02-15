package com.zhw.skynet;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.core.ServiceMeta;
import com.zhw.skynet.core.body.AvroBodyMapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Metas {
    public static List<ServiceMeta> of(Class<?> clz) {
        Set<String> names = new HashSet<>();
        String name = clz.getName();
        Method[] methods = clz.getDeclaredMethods();
        List<ServiceMeta> metas = new ArrayList<>(methods.length);
        for (Method method : methods) {
            String serviceName = name + Constants.SERVICE_NAME_SEPARATOR + method.getName();
            if (!names.add(serviceName)) {
                throw new IllegalStateException("serviceName exists " + serviceName);
            }
            ServiceMeta meta = new ServiceMeta();
            meta.setServiceName(serviceName);
            meta.setRequestMapper(new AvroBodyMapper(method.getGenericParameterTypes()[0]));
            meta.setResponseMapper(new AvroBodyMapper(method.getGenericReturnType()));
            metas.add(meta);
        }
        return metas;
    }
}
