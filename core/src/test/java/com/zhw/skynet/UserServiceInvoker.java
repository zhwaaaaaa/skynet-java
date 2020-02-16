package com.zhw.skynet;

import com.zhw.skynet.core.ServiceInvoker;
import com.zhw.skynet.core.ServiceMeta;
import com.zhw.skynet.service.User;
import com.zhw.skynet.service.UserService;

import java.util.List;

public class UserServiceInvoker implements ServiceInvoker {
    private UserService userService;
    private ServiceMeta meta;

    public UserServiceInvoker(UserService userService) {
        this.userService = userService;
        meta = Metas.of(UserService.class).get(0);
    }

    @Override
    public ServiceMeta getServiceMeta() {
        return meta;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object param) throws Throwable {
        return userService.tellDesc((List<User>) param);
    }
}
