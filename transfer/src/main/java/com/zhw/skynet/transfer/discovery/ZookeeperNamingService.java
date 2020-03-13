package com.zhw.skynet.transfer.discovery;

import com.zhw.skynet.transfer.ServiceDesc;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ZookeeperNamingService implements NamingService {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(ZookeeperNamingService.class);

    private CuratorFramework client;
    private Map<String, NotifyListener> failSubscribe = new ConcurrentHashMap<>();
    private Map<String, PathChildrenCache> failUnsubscribe = new ConcurrentHashMap<>();
    private Map<String, ServiceDesc> failRegister = new ConcurrentHashMap<>();
    private Set<String> failUnregister = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Map<String, PathChildrenCache> subscribeCacheMap = new ConcurrentHashMap<>();

    private String registerIp;
    private int registerPort;

    public ZookeeperNamingService(String urlStr, String registerIp, int registerPort) {
        this.registerIp = registerIp;
        this.registerPort = registerPort;
        SkynetURL url = new SkynetURL(urlStr);
        int sleepTimeoutMs = url.getIntParameter("sleepTimeoutMs", 2000);
        int maxTryTimes = url.getIntParameter("maxTryTimes", 3000);
        int sessionTimeoutMs = url.getIntParameter("sessionTimeoutMs", 5000);
        int connectTimeoutMs = url.getIntParameter("connectTimeoutMs", 3000);
        String namespace = "skynet";
        if (url.getPath().startsWith("/")) {
            namespace = url.getPath().substring(1);
        }
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(sleepTimeoutMs, maxTryTimes);
        client = CuratorFrameworkFactory.builder()
                .connectString(url.getHostPorts())
                .connectionTimeoutMs(connectTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .retryPolicy(retryPolicy)
                .namespace(namespace)
                .build();
        client.start();

    }


    @Override
    public List<ServiceInstance> lookup(String service) {

        try {
            List<String> ips = client.getChildren().forPath(service);

            return ips.stream()
                    .filter(x -> x.contains(":"))
                    .map(x -> getServiceInstance(service, x))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private ServiceInstance getServiceInstance(String service, String ipPort) {
        String[] split = ipPort.split(":");
        return new ServiceInstance(split[0], Integer.parseInt(split[1]), service);
    }

    @Override
    public void subscribe(String service, NotifyListener listener) {
        PathChildrenCache cache = new PathChildrenCache(client, service, true);
        cache.getListenable().addListener((client, event) -> {
            ChildData data = event.getData();
            switch (event.getType()) {
                case CHILD_ADDED: {
                    String path = data.getPath();
                    String ipPort = path.substring(path.lastIndexOf('/') + 1);
                    listener.notifyAdd(Collections.singletonList(getServiceInstance(service, ipPort)));
                    break;
                }
                case CHILD_REMOVED: {
                    String path = data.getPath();
                    String ipPort = path.substring(path.lastIndexOf('/') + 1);
                    listener.notifyDel(Collections.singletonList(getServiceInstance(service, ipPort)));
                    break;
                }
                case CHILD_UPDATED:
                    break;
                default:
                    break;
            }
        });
        try {
            cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            subscribeCacheMap.put(service, cache);
            failUnsubscribe.remove(service);
            log.info("subscribe success from {}", service);
        } catch (Exception e) {
            failSubscribe.put(service, listener);
        }
    }

    @Override
    public void unsubscribe(String service) {
        failSubscribe.remove(service);
        PathChildrenCache cache = subscribeCacheMap.remove(service);
        try {
            if (cache != null) {
                cache.close();
            }
            log.info("unsubscribe success from {}", service);
        } catch (Exception ex) {
            failUnsubscribe.put(service, cache);
        }
    }

    @Override
    public void register(ServiceDesc serviceDesc) {
        String serviceName = serviceDesc.getServiceName();
        failUnregister.remove(serviceName);

        String path = serviceName + "/" + registerIp + ":" + registerPort;
        try {
            if (client.checkExists().forPath(serviceName) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(serviceName);
            }
            if (client.checkExists().forPath(path) != null) {
                try {
                    client.delete().forPath(path);
                } catch (Exception deleteException) {
                    log.info("zk delete node failed, ignore");
                }
            }
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            bs.write(serviceDesc.getParam().getBytes());
            bs.write(serviceDesc.getResult().getBytes());
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path, bs.toByteArray());
            log.info("register success to {}", path);
            failRegister.remove(serviceName);
        } catch (Exception ex) {
            failRegister.put(serviceName, serviceDesc);
        }
    }

    @Override
    public void unregister(String service) {
        failRegister.remove(service);
        String path = service + "/" + registerIp + ":" + registerPort;
        try {
            client.delete().guaranteed().forPath(path);
            log.info("unregister success to {}", path);
            failUnregister.remove(service);
        } catch (Exception ex) {
            failUnregister.add(service);
        }
    }

    @Override
    public void destroy() {
        client.close();
    }
}
