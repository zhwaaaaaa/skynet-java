package com.zhw.skynet;

import com.zhw.skynet.core.Client;
import com.zhw.skynet.core.Request;
import com.zhw.skynet.core.Response;
import com.zhw.skynet.core.ServiceMeta;
import com.zhw.skynet.service.User;
import com.zhw.skynet.service.UserService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestClient {
    public static void main(String[] args) throws Throwable {
        Client client = new Client("ubuntu", 9997, Executors.newFixedThreadPool(4));
        List<ServiceMeta> of = Metas.of(UserService.class);
        client.start(of);
        System.out.println("服务调用方启动成功");
        ServiceMeta meta = of.get(0);
        long start;
        int times = 10000;
        /*List<User> users = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            users.add(new User("王宝强" + i, i, i % 2 == 0));
        }
        long start = System.currentTimeMillis();
        Response response = client.send(new Request(meta, users));
        if (response.getErr() != null) {
            response.getErr().printStackTrace(System.out);
        }
        System.out.println(response);
        System.out.println("1次同步调用服务，耗时" + (System.currentTimeMillis() - start));*/
        times = 300000;
        CountDownLatch latch = new CountDownLatch(times);
        start = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            Request req = new Request(meta, Collections.singletonList(
                    new User("王宝强" + i, i, i % 2 == 1)));
            client.sendAsync(req,
                    (r, e) -> {
                        latch.countDown();
                        if (e != null) {
                            System.out.println("error in invoke " + req.getReqId() + " " + e.getMessage());
                        } else {
                            System.out.println(r);
                        }
                    }
            );
        }
        latch.await();
        System.out.println(latch.getCount());
        System.out.println(times + "次异步调用服务，耗时" + (System.currentTimeMillis() - start));
    }
}
