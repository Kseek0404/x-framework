package de.kseek.core.curator;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import de.kseek.core.config.ZookeeperConfig;
import de.kseek.core.constant.XConstant;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 主要专注于 ZooKeeper 操作和事件通知
 * @author kseek
 * @date 2024/3/22
 */
@Order(3)
@Component
@Slf4j
@RequiredArgsConstructor
public class XCurator implements ApplicationListener<ContextRefreshedEvent>,
        ApplicationRunner, CuratorCacheListener, ConnectionStateListener, DisposableBean {
    public static final String ALL = "ALL";

    private final ZookeeperConfig zkConfig;

    @Getter
    private CuratorFramework client = null;

    private String rootPath;

    private String nodePath;

    private XNode xRootNode;

    private CuratorCache curatorCache;

    private Set<XCuratorListener> listeners = new CopyOnWriteArraySet<>();

    private final Map<String, Set<XNodeListener>> xNodeListeners = new HashMap<>();

    /**
     * 初始化所有xNodeListener
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        Map<String, XCuratorListener> tmpMap = context.getBeansOfType(XCuratorListener.class);
        if (!tmpMap.isEmpty()) {
            listeners = new CopyOnWriteArraySet<>(tmpMap.values());
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 初始化ZK的client
            log.debug("x curator init. ");
            ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(zkConfig.getBaseSleepTimeMs(), zkConfig.getMaxRetries());
            client = CuratorFrameworkFactory.newClient(zkConfig.getConnects(), zkConfig.getSessionTimeoutMs(), zkConfig.getConnectionTimeoutMs(), retryPolicy);
            client.start();
            client.blockUntilConnected();
            // 初始化根节点
            initRootPath();
            // 启动Zk的Curator缓存
            cacheXNode();
        } catch (Exception e) {
            log.warn("x curator init fail...", e);
        }
    }

    private void initRootPath() {
        try {
            rootPath = XConstant.SEPARATOR + zkConfig.getXRoot();
            if (!checkExists(rootPath)) {
                log.debug("x root path {} not exist.", rootPath);
                client.create().forPath(rootPath);
            }
        } catch (Exception e) {
            log.warn("x  root path init fail...", e);
        }
    }

    public void stop() {
        try {
            if (this.curatorCache != null) {
                log.debug("treeCache 关闭");
                this.curatorCache.close();
                log.debug("treeCache 已关闭");
            }
            if (this.client != null) {
                log.debug("client 关闭");
                this.client.close();
                log.debug("client 已关闭");
            }
        } catch (Exception var2) {
            log.warn("x curator stop fail.", var2);
        }
    }

    public void restart() {
        try {
            stop();
            run(null);
        } catch (Exception e) {
            log.warn("restart fail.", e);
        }
    }

    /**
     * 锁服务，获得一个指定路径的锁
     *
     * @param lockPath
     * @return
     */
    public InterProcessMutex getLock(String lockPath) {
        return new InterProcessMutex(client, lockPath);
    }

    public String addPath(String path, byte[] payload, boolean persistent) {
        nodePath = mkPath(path);
        try {
            if (!checkExists(nodePath)) {
                log.info(" XCurator create path {}", nodePath);
                client.create()
                        .creatingParentsIfNeeded().withMode(persistent
                                ? CreateMode.PERSISTENT : CreateMode.EPHEMERAL).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(nodePath, payload);
            }
        } catch (Exception e) {
            log.warn("add path fail.path is {}", path, e);
        }
        return nodePath;
    }

    public String updatePath(String path, byte[] payload, boolean persistent) {
        nodePath = mkPath(path);
        try {
            if (checkExists(nodePath)) {
                log.info("update path {}", nodePath);
                client.setData().forPath(nodePath, payload);
            }
        } catch (Exception e) {
            log.warn("update path fail.path is {}", path, e);
        }
        return nodePath;
    }

    public void addXNodeListener(String path, XNodeListener listener) {
        logXNodeListenerOperation("add", path, listener);
        Set<XNodeListener> set = xNodeListeners.computeIfAbsent(path, e -> new HashSet<>());
        set.add(listener);
    }

    public void removeXNodeListener(String path, XNodeListener listener) {
        logXNodeListenerOperation("remove", path, listener);
        Set<XNodeListener> set = xNodeListeners.computeIfAbsent(path, e -> new HashSet<>());
        set.remove(listener);
    }

    /**
     * 以根节点添加监听
     *
     * @param listener
     */
    public void addXNodeListener(XNodeListener listener) {
        logXNodeListenerOperation("add", ALL, listener);
        Set<XNodeListener> set = xNodeListeners.computeIfAbsent(ALL, e -> new HashSet<>());
        set.add(listener);
    }

    /**
     * 统一的监听器操作日志记录
     */
    private void logXNodeListenerOperation(String operation, String path, XNodeListener listener) {
        log.info("{} x node listener.path={},listener_type={}", operation, path, listener.getClass().getSimpleName());
    }

    private void notifyXNodeListener(XNodeListener.NodeChangeType nodeChangeType, XNode xNode) {
        String path = xNode.getNodePath();
        log.debug("notify x node listeners.path={},nodeChangeType={}", path, nodeChangeType);
        notifyListenersForPath(path, nodeChangeType, xNode);
        if ((nodePath == null || !nodePath.equals(path)) && !rootPath.equals(path) && xNode.getNodeConfig() != null) {
            notifyListenersForPath(ALL, nodeChangeType, xNode);
        }
    }

    /**
     * 通知指定路径的所有监听器
     */
    private void notifyListenersForPath(String path, XNodeListener.NodeChangeType nodeChangeType, XNode xNode) {
        Set<XNodeListener> set = xNodeListeners.computeIfAbsent(path, e -> new HashSet<>());
        set.forEach(listener -> {
            listener.nodeChange(nodeChangeType, xNode);
            log.info("notify x node listeners.path={},nodeChangeType={},listener={}", path, nodeChangeType, listener);
        });
    }


    public XNode getXNode(String path) {
        //path = mkPath(path);
        return xRootNode.getChildren(path, true);
    }

    private void addXNode(String path, String data, String strStat) {
        XNode xNode = new XNode(path, data, strStat);
        if (path.equals(rootPath)) {
            xRootNode = xNode;
        } else {
            xRootNode.addChildren(xNode);
        }
        notifyXNodeListener(XNodeListener.NodeChangeType.NODE_ADD, xNode);
    }

    private void updateXNode(String path, String data, String strStat) {
        XNode xNode = this.xRootNode.getChildren(path, true);
        if (xNode != null) {
            xNode.updateData(data);
            xNode.setStrStat(strStat);
        }

    }

    private XNode removeXNode(String path) {
        XNode xNode = xRootNode.removeChildren(path);
        if (xNode != null) {
            notifyXNodeListener(XNodeListener.NodeChangeType.NODE_REMOVE, xNode);
        } else {
            log.warn("[NODE_REMOVE],找不到指定的节点,path={}", path);
        }
        return xNode;
    }

    private void cacheXNode() {
        curatorCache = CuratorCache.build(client, rootPath);
        curatorCache.listenable().addListener(this);
        curatorCache.start();
    }

    private boolean checkExists(String path) throws Exception {
        Stat stat = client.checkExists().forPath(path);
        return stat != null;
    }

    private String mkPath(String path) {
        return rootPath + path;
    }

    private void notifyRefreshed() {
        listeners.forEach(listener -> {
            try {
                listener.xCuratorRefreshed(this);
            } catch (Exception e) {
                log.warn("notify refreshed error.", e);
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        log.info("监听到节点关闭");
        stop();
    }

    @SneakyThrows
    @Override
    public void event(Type type, ChildData oldData, ChildData childData) {
        ChildData child = null;
        if (oldData != null) {
            child = oldData;
        }
        if (childData != null) {
            child = childData;
        }
        if (child == null) {
            log.info("[节点控制] 收到节点事件, 但无法处理, data都为空: " + "type={}", type);
            return;
        }
        byte[] byteData = child.getData();
        if (byteData == null) {
            byteData = new byte[0];
        }
        String data = new String(byteData, StandardCharsets.UTF_8);
        String strStat = JSON.toJSONString(child.getStat());
        String path = child.getPath();
        log.info("[节点控制] 收到节点事件: type=[{}], path={}, info={}, stat={}", type, path, data, strStat);
        switch (type) {
            case NODE_CREATED:
                addXNode(path, data, strStat);
                break;
            case NODE_CHANGED:
                updateXNode(path, data, strStat);
                break;
            case NODE_DELETED:
                removeXNode(path);
                break;
        }
    }

    @Override
    public void initialized() {
        notifyRefreshed();
    }

    @SneakyThrows
    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        switch (newState) {
            case RECONNECTED:
                restart();
                break;
            case LOST:
                if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                    restart();
                }
                break;
        }
    }
}
