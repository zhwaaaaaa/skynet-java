package com.zhw.skynet.transfer;

import com.zhw.skynet.transfer.discovery.NamingService;
import com.zhw.skynet.transfer.discovery.NotifyListener;
import com.zhw.skynet.transfer.discovery.ServiceInstance;
import io.netty.buffer.ByteBuf;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ConsumerChannelGroup implements NotifyListener, RemoteEndPointGroup {
    private final NamingService namingService;
    private final String service;
    private final RemoteServerConnectionFactory endPointFactory;
    private int index;
    private Set<Integer> careCh = new HashSet<>();

    private List<RemoteServerConnection> endPoints = new CopyOnWriteArrayList<>();

    public ConsumerChannelGroup(NamingService namingService, String service,
                                RemoteServerConnectionFactory endPointFactory) {
        this.namingService = namingService;
        this.service = service;
        this.endPointFactory = endPointFactory;
        notifyAdd(namingService.lookup(service));
        namingService.subscribe(service, this);
    }

    @Override
    public String getServiceName() {
        return service;
    }

    @Override
    public boolean send(ByteBuf msg) {
        if (endPoints.size() == 0) {
            return false;
        }
        int size = endPoints.size();

        for (int i = 0; i < size; i++) {
            int c = index++;
            if (c < 0) {
                index = 0;
            }
            RemoteServerConnection connection = endPoints.get(c % size);
            if (connection.send(msg)) {
                return true;
            }
        }

        return false;
    }

    public void addCareId(Integer id) {
        careCh.add(id);
    }

    public void removeCareId(Integer id) {
        careCh.remove(id);
    }

    @Override
    public int subscribeSize() {
        return careCh.size();
    }

    @Override
    public void close() {
        namingService.unsubscribe(service);
        for (RemoteServerConnection conn : endPoints) {
            closeConn(conn);
        }
    }

    @Override
    public void notifyAdd(Collection<ServiceInstance> addList) {
        if (addList.isEmpty()) {
            return;
        }
        List<RemoteServerConnection> endPoints = addList.stream()
                .map(x -> {
                    RemoteServerConnection connection = endPointFactory.getOrCreateConn(x.getIp(), x.getPort());
                    connection.addRelateService(x.getServiceName());
                    return connection;
                })
                .collect(Collectors.toList());
        this.endPoints.addAll(endPoints);
    }

    @Override
    public void notifyDel(Collection<ServiceInstance> deleteList) {
        Iterator<RemoteServerConnection> iterator = endPoints.iterator();
        while (iterator.hasNext()) {
            RemoteServerConnection next = iterator.next();
            for (ServiceInstance instance : deleteList) {
                if (next.matchIpPort(instance.getIp(), instance.getPort())) {
                    iterator.remove();
                    closeConn(next);
                    break;
                }
            }
        }
    }

    private void closeConn(RemoteServerConnection conn) {
        int i = conn.removeRelateService(service);
        if (i == 0) {
            conn.close();
        }
    }
}
