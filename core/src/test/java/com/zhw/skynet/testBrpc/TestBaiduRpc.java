package com.zhw.skynet.testBrpc;

import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.RpcClientOptions;
import com.baidu.brpc.client.instance.Endpoint;
import com.baidu.brpc.client.loadbalance.LoadBalanceStrategy;
import com.baidu.brpc.naming.NamingOptions;
import com.baidu.brpc.protocol.Options;
import example.EchoRequest;
import example.EchoService;

import java.util.Collections;

public class TestBaiduRpc {
    public static void main(String[] args) {
        RpcClientOptions clientOption = new RpcClientOptions();
        clientOption.setProtocolType(Options.ProtocolType.PROTOCOL_BAIDU_STD_VALUE);
        clientOption.setWriteTimeoutMillis(1000);
        clientOption.setReadTimeoutMillis(5000);
        clientOption.setMaxTotalConnections(1);
        clientOption.setMinIdleConnections(1);
        clientOption.setLoadBalanceType(LoadBalanceStrategy.LOAD_BALANCE_FAIR);
        clientOption.setCompressType(Options.CompressType.COMPRESS_TYPE_NONE);

        RpcClient client = new RpcClient(new Endpoint("ubuntu", 8000), clientOption);

        EchoService echoService = BrpcProxy.getProxy(client, EchoService.class);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 20000; i++) {
            long ss = System.nanoTime();
            echoService.Echo(new EchoRequest("EchoResponse1")).getMessage();
            System.out.println("花费时间:" + (System.nanoTime() - ss));
        }
        System.out.println("总花费时间:" + (System.currentTimeMillis() - start));
        client.stop();
    }
}
