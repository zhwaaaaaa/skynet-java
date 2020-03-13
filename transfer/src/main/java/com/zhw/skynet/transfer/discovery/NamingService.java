package com.zhw.skynet.transfer.discovery;

import com.zhw.skynet.transfer.ServiceDesc;

import java.util.List;

public interface NamingService {

    /**
     * 查询符合条件的已注册数据，与订阅的推模式相对应，这里为拉模式，只返回一次结果。
     *
     * @param service service/group/version info
     * @return 已注册信息列表，可能为空。
     */
    List<ServiceInstance> lookup(String service);

    /**
     * 订阅符合条件的已注册数据，当有注册数据变更时自动推送.
     *
     * @param listener 变更事件监听器，不允许为空
     */
    void subscribe(String service, NotifyListener listener);

    /**
     * 取消订阅.
     */
    void unsubscribe(String service);

    /**
     * 注册数据，比如：提供者地址，消费者地址，路由规则，覆盖规则，等数据。
     *
     * @param serviceDesc service/group/version info
     */
    void register(ServiceDesc serviceDesc);

    /**
     * 取消注册.
     *
     * @param service service/group/version info
     */
    void unregister(String service);

    void destroy();

}
