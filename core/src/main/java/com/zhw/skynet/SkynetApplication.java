package com.zhw.skynet;

import com.zhw.skynet.core.Client;
import com.zhw.skynet.core.Request;
import com.zhw.skynet.core.ServiceMeta;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class SkynetApplication {
    public static void main(String[] args) throws Throwable {
        Client client = new Client("ubuntu", 9999, Runnable::run);
        ServiceMeta meta = new ServiceMeta("com.zhw.service.PrintService.print");
        client.start(Collections.singletonList(meta));

        long bs = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            long start = System.nanoTime();
            Request req = new Request(meta, null);
            client.send(req);
            System.out.println(req.getReqId() + "花费时间:" + (System.nanoTime() - start));
        }
        System.out.println("总花费时间:" + (System.currentTimeMillis() - bs));
        int asyncNum = 10000;
        CountDownLatch latch = new CountDownLatch(asyncNum);
        bs = System.currentTimeMillis();
        for (int i = 0; i < asyncNum; i++) {
            Request req = new Request(meta, null);
            client.sendAsync(req, (r, e) -> {
                if (e != null) {
                    e.printStackTrace(System.out);
                }
                latch.countDown();
            });
        }
        latch.await();
        System.out.println("总花费时间:" + (System.currentTimeMillis() - bs));
    }
}
