package com.zhw.skynet.core;

import com.zhw.skynet.common.Constants;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

public interface EndPoint {

    static void validServiceMetas(Collection<ServiceMeta> serviceMetas) {
        if (CollectionUtils.isEmpty(serviceMetas)) {
            throw new IllegalArgumentException("requireServices required");
        }
        if (serviceMetas.stream().map(ServiceMeta::getServiceName).distinct().count()
                < serviceMetas.size()) {
            throw new IllegalArgumentException("service name duplicate");
        }

        if (serviceMetas.size() > Constants.MAX_SERVICE_SIZE) {
            throw new IllegalArgumentException("requireServices max size " + Constants.MAX_SERVICE_SIZE);
        }
    }

    void close();
}
