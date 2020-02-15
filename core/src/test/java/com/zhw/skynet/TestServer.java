package com.zhw.skynet;

import com.zhw.skynet.core.Server;
import com.zhw.skynet.service.UserServiceImp;

import java.util.concurrent.Executors;

public class TestServer {
    public static void main(String[] args) throws Throwable {
        Server server = new Server("ubuntu", 9998,
                Executors.newCachedThreadPool());
        server.addInvoker(new UserServiceInvoker(new UserServiceImp()));
        server.start();
        System.out.println("服务接受方启动成功");
    }
}
