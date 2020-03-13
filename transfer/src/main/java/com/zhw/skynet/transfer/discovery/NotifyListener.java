package com.zhw.skynet.transfer.discovery;

import java.util.Collection;

/**
 * NotifyListener. (API, Prototype, ThreadSafe)
 *
 * @author xiemalin
 * @since 2.27
 */
public interface NotifyListener {

    void notifyAdd(Collection<ServiceInstance> addList);

    void notifyDel(Collection<ServiceInstance> deleteList);
}