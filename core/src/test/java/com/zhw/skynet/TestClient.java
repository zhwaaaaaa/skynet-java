package com.zhw.skynet;

import com.zhw.skynet.core.Response;
import com.zhw.skynet.core.client.Client;
import com.zhw.skynet.core.Request;
import com.zhw.skynet.core.ServiceMeta;
import com.zhw.skynet.service.User;
import com.zhw.skynet.service.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class TestClient {
    public static void main(String[] args) throws Throwable {
        Client client = new Client("ubuntu", 9997, Executors.newFixedThreadPool(4));
        List<ServiceMeta> of = Metas.of(UserService.class);
        client.start(of);
        System.out.println("服务调用方启动成功");
        ServiceMeta meta = of.get(0);
        meta.setTimeoutMs(100000);
        sendSync(client, meta, 100, 1);
        sendSync(client, meta, 100, 10000);
        sendSync(client, meta, 10000, 1);
        sendSync(client, meta, 10000, 10);
//        times = 3;
        asyncSend(client, meta, 100, 1);
        asyncSend(client, meta, 100, 100000);
        asyncSend(client, meta, 10000, 1);
        asyncSend(client, meta, 100000, 100);
        asyncSend(client, meta, 1000000, 1);

    }

    private static void asyncSend(Client client, ServiceMeta meta, int times, int bodySize) throws InterruptedException {
        List<User> users = new ArrayList<>(bodySize);
        for (int i = 0; i < bodySize; i++) {
            users.add(new User("王宝强" + i, i, i % 2 == 0));
        }

        CountDownLatch latch = new CountDownLatch(times);
        long start = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            Request req = new Request(meta, users);
            client.sendAsync(req,
                    (r, e) -> {
                        latch.countDown();
                        if (e != null) {
                            System.out.println("error in invoke " + req.getReqId() + " " + e.getMessage());
                        } else if (r.getCode() != 0) {
                            System.out.println(r);
                        }
                    }
            );
        }
        latch.await();
        System.out.println(latch.getCount());
        System.out.println(times + "次异步调用服务" + bodySize + "，耗时" + (System.currentTimeMillis() - start));
    }

    private static void sendSync(Client client, ServiceMeta meta, int times, int bodySize) {
        List<User> users = new ArrayList<>(bodySize);
        for (int i = 0; i < bodySize; i++) {
            users.add(new User("王宝强" + i, i, i % 2 == 0));
        }
        long start = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            Response response = client.send(new Request(meta, users));
            if (response.getErr() != null) {
                response.getErr().printStackTrace(System.out);
            } else if (response.getCode() != 0) {
                System.out.println(response);
            }
        }

        System.out.println(times + "次同步调用服务" + bodySize + "，耗时" + (System.currentTimeMillis() - start));
    }
}
