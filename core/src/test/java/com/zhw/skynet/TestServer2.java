package com.zhw.skynet;

import com.zhw.skynet.core.server.Server;
import com.zhw.skynet.service.UserServiceImp2;

import java.util.concurrent.Executors;

public class TestServer2 {
    public static void main(String[] args) throws Throwable {
        Server server = new Server("ubuntu", 9998,
                Executors.newFixedThreadPool(4));
        server.addInvoker(new UserServiceInvoker(new UserServiceImp2()));
        server.start();
        System.out.println("服务接受方启动成功");
    }
}
