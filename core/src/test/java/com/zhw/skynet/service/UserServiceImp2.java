package com.zhw.skynet.service;

import java.util.List;

public class UserServiceImp2 implements UserService {
    @Override
    public String tellDesc(List<User> users) {
        System.out.println("服务被调用：" + users);
        return "这是服务器2给你回的消息：" + users.toString();
    }
}
