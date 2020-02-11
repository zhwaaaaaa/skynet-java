package com.zhw.skynet;

import com.zhw.skynet.core.Client;
import com.zhw.skynet.core.Request;

import java.util.Collections;

public class SkynetApplication {
    public static void main(String[] args) throws Throwable {
        Client client = new Client("ubuntu", 9999);
        String service = "com.zhw.service.PrintService.print";
        client.start(Collections.singletonList(service));
        client.send(new Request(service, null));
    }
}
