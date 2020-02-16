package com.zhw.skynet.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UserServiceImp1 implements UserService {

    private AtomicInteger times = new AtomicInteger(0);

    @Override
    public String tellDesc(List<User> users) {
        System.out.println(times.incrementAndGet());
        return "这是服务器1收到:" + users.size();
    }
}
