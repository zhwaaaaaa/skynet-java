package com.zhw.skynet;

import com.zhw.skynet.core.Client;
import com.zhw.skynet.core.Request;
import com.zhw.skynet.core.Response;

import java.util.Collections;

public class SkynetApplication {
    public static void main(String[] args) throws Throwable {
        Client client = new Client("ubuntu", 9999);
        String service = "com.zhw.service.PrintService.print";
        client.start(Collections.singletonList(service));
        long bs = System.currentTimeMillis();
        int i = 0;
        try {
            for (; i < 1000000; i++) {
                long start = System.nanoTime();
                Request req = new Request(service, null);
                Response response = client.send(req);
                System.out.println(req.getReqId() + "花费时间:" + (System.nanoTime() - start));
                response.getBody().release();
            }
            System.out.println("总花费时间:" + (System.currentTimeMillis() - bs));
        } catch (Exception e) {
            System.out.println(i);
            e.printStackTrace(System.out);
        }

    }
}
