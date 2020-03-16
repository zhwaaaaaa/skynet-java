package com.zhw.skynet;

import com.zhw.skynet.core.server.Server;
import com.zhw.skynet.service.UserServiceImp1;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class TestServer1 {
    public static void main(String[] args) throws Throwable {
        Server server = new Server("ubuntu", 9998,
                Executors.newFixedThreadPool(8));
        UserServiceInvoker invoker = new UserServiceInvoker(new UserServiceImp1());
        server.addInvoker(invoker);
        server.start();
        System.out.println("服务接受方启动成功");
    }
}
