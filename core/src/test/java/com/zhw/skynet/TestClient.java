package com.zhw.skynet;

import com.zhw.skynet.core.Client;
import com.zhw.skynet.core.Request;
import com.zhw.skynet.core.Response;
import com.zhw.skynet.core.ServiceMeta;
import com.zhw.skynet.service.User;
import com.zhw.skynet.service.UserService;

import java.util.List;
import java.util.concurrent.Executors;

public class TestClient {
    public static void main(String[] args) throws Throwable {
        Client client = new Client("ubuntu", 9997, Executors.newFixedThreadPool(2));
        List<ServiceMeta> of = Metas.of(UserService.class);
        client.start(of);
        System.out.println("服务调用方启动成功");
        Response response = client.send(new Request(of.get(0), new User("王宝强", 19, false)));
        if (response.getErr() != null) {
            response.getErr().printStackTrace(System.out);
        }
        System.out.println(response);

    }
}
