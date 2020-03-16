package com.zhw.skynet.core.sh;

import com.zhw.skynet.core.Action;
import com.zhw.skynet.core.ServiceMeta;

import java.util.Collection;

public class ShakeHandsReq extends Action<ShakeHandsResp> {


    private Collection<ServiceMeta> services;

    public ShakeHandsReq(Collection<ServiceMeta> services) {
        this.services = services;
    }

    public Collection<ServiceMeta> getServices() {
        return services;
    }
}
