package com.zhw.skynet.service;

public class UserServiceImp implements UserService {
    @Override
    public String tellDesc(User user) {
        System.out.println("服务被调用：" + user);
        return "这是服务器给你回的消息" + user.toString();
    }
}
