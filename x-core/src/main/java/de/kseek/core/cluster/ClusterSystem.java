package de.kseek.core.cluster;

import com.alibaba.fastjson2.JSONObject;
import de.kseek.core.curator.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.system.ApplicationPid;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import de.kseek.core.config.NodeConfig;
import de.kseek.core.gate.GateClusterMessageDispatcher;
import de.kseek.core.message.SwitchNodeMessage;
import de.kseek.core.net.Connect;
import de.kseek.core.net.NetAddress;
import de.kseek.core.netty.ConnectPool;
import de.kseek.core.netty.NettyServer;
import de.kseek.core.protostuff.PFSession;
import de.kseek.core.timer.TimerCenter;
import de.kseek.core.timer.TimerEvent;
import de.kseek.core.timer.TimerListener;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class ClusterSystem implements XNodeListener, ApplicationListener<ContextRefreshedEvent>, ApplicationRunner, TimerListener {

    private static volatile ClusterSystem system;

    /**
     * 获取 ClusterSystem 实例
     * 
     * @return ClusterSystem 实例
     * @throws IllegalStateException 如果实例尚未初始化
     */
    public static ClusterSystem getInstance() {
        ClusterSystem instance = system;
        if (instance == null) {
            throw new IllegalStateException("ClusterSystem has not been initialized yet. Please ensure ApplicationRunner.run() has been executed.");
        }
        return instance;
    }

    /**
     * 集群各个节点客户端对象
     */
    private final Map<XNode, ClusterClient> clusterClientMap = new HashMap<>();

    /**
     * 玩家session集合
     */
    private final Map<String, PFSession> sessionMap = new ConcurrentHashMap<>();

    private ClusterMessageDispatcher clusterMessageDispatcher;

    /**
     * 连接通道初始化器
     */
    private ClusterConnectInitializer initializer;

    /**
     * 多线程连接通道初始化器
     */
    private ClusterConnectWorkPoolInitializer workPoolInitializer;

    /**
     * 微服务消息节点索引
     */
    private final Map<Integer, List<XNode>> micServiceIndexs = new HashMap<>();

    private final NodeManager nodeManager;
    private final XCurator xCurator;
    /**
     * 本节点的配置信息
     */
    private final NodeConfig localNodeConfig;
    private final Optional<TimerCenter> timerCenter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        system = this; // 初始化静态实例
        startClusterServer();
        xCurator.addXNodeListener(this);
        timerCenter.ifPresent(
                t -> t.add(new TimerEvent<>(this, "ClusterSystem", 1).setInitTime(10).withTimeUnit(TimeUnit.MINUTES)));
    }

    @Bean
    public ClusterMessageDispatcher clusterMessageDispatcher() {
        ClusterMessageDispatcher dispatcher;
        if (NodeType.GATE == nodeManager.getNodeType()) {
            dispatcher = new GateClusterMessageDispatcher();
        } else {
            dispatcher = new ClusterMessageDispatcher();
        }
        // 使用 setter 注入避免循环依赖
        dispatcher.setClusterSystem(this);
        dispatcher.setPkgs(localNodeConfig.messagePkgs);
        clusterMessageDispatcher = dispatcher;
        return dispatcher;
    }

    @Bean
    public ClusterConnectInitializer createClusterConnectInitializer(ClusterMessageDispatcher dispatcher) {
        String nodePath = getNodePath();
        initializer = new ClusterConnectInitializer(dispatcher, nodePath);
        workPoolInitializer = new ClusterConnectWorkPoolInitializer(dispatcher, nodePath);
        return initializer;
    }

    public Map<String, PFSession> sessionMap() {
        return sessionMap;
    }

    /**
     * 移除session
     *
     * @param sessionId
     * @return
     */
    public PFSession removeSession(String sessionId) {
        return sessionMap.remove(sessionId);
    }

    /**
     * 切换到固定节点
     *
     * @param xNode
     */
    public void switchNode(PFSession pfSession, XNode xNode) {
        log.info("切换节点，sessionId={},toNode={}", pfSession.getSessionId(), xNode.getNodePath());
        try {
            SwitchNodeMessage switchNodeMessage = new SwitchNodeMessage(pfSession.getSessionId(), xNode.getNodePath(), pfSession.userId);
            pfSession.send2Gate(switchNodeMessage);
        } catch (Exception e) {
            log.warn("节点切换异常", e);
        }
    }

    /**
     * 随机切换到指定类型的节点
     *
     * @param nodeType
     */
    public XNode switchNode(PFSession pfSession, NodeType nodeType) {
        try {
            XNode xNode = randomXNode(nodeType, pfSession.getAddress().getHost(), pfSession.userId);
            switchNode(pfSession, xNode);
            return xNode;
        } catch (Exception e) {
            log.warn("节点切换异常", e);
        }
        return null;
    }

    /**
     * 随机切换到指定类型的节点
     *
     * @param nodePath
     */
    public XNode switchNode(PFSession pfSession, String nodePath) {
        try {
            XNode xNode = getNode(nodePath);
            switchNode(pfSession, xNode);
            return xNode;
        } catch (Exception e) {
            log.warn("节点切换异常", e);
        }
        return null;
    }

    public String getNodePath() {
        return nodeManager.getXNodePath(localNodeConfig.getType(), localNodeConfig.getName());
    }

    public ClusterClient getByNodeType(NodeType nodeType, String ip, long id) {
        XNode randomOneXNode = randomXNode(nodeType, ip, id);
        if (randomOneXNode == null) {
            return null;
        }
        return clusterClientMap.get(randomOneXNode);
    }

    public XNode randomXNode(NodeType nodeType, String ip, long id) {
        XNode xNode = nodeManager.getXNode(nodeType);
        if (xNode == null || !xNode.hasChildren()) {
            log.warn("node not found or not has children,tpye is {}", nodeType);
            return null;
        }
        return xNode.randomOneXNodeWithWeight(ip, id);
    }

    /**
     * 获取所有网关节点
     *
     * @return
     */
    public List<ClusterClient> getAllGate() {
        List<ClusterClient> clusterClients = new ArrayList<>();
        clusterClientMap.values().forEach(clusterClient -> {
            if (NodeType.GATE.toString().equals(clusterClient.getType())) {
                clusterClients.add(clusterClient);
            }
        });
        return clusterClients;
    }

    public XNode getNode(String path) {
        if (path == null) {
            log.warn("节点查找异常，path=null");
            return null;
        }
        return xCurator.getXNode(path);
    }

    public ClusterClient getClusterByPath(String path) {
        XNode xNode = xCurator.getXNode(path);
        if (xNode == null) {
            log.warn("node not found ,path is {}", path);
            return null;
        }
        return clusterClientMap.get(xNode);
    }

    public ConnectPool getXConnectPool(NetAddress netAddress) {
        return new ConnectPool(netAddress, initializer, localNodeConfig.getClusterConnectPoolSize())
                .init().start(timerCenter.orElse(null));
    }

    public void startClusterServer() {
        NetAddress netAddress = localNodeConfig.getTcpAddress();
        NettyServer nettyServer = new NettyServer(netAddress.getPort(), localNodeConfig.isWorkPool() ? workPoolInitializer : initializer);
        nettyServer.setName("tcp-nio-" + netAddress.getPort());
        nettyServer.start();
    }

    private void nodeAdd(XNode xNode) {
        // leader不进行处理
        if (xNode.getNodeConfig() != null && StringUtils.isNotEmpty(xNode.getNodeConfig().getType())) {
            NodeType nodeType = NodeType.valueOf(xNode.getNodeConfig().getType());
            if (nodeType == NodeType.LEADER) {
                log.debug("nodeAdd leader类型不进行处理!nodeConfig:{}", JSONObject.toJSONString(xNode.getNodeConfig()));
                return;
            }
        }

        // 仅网关主动连接其他类型节点；Login/Hall 等后端只接受 Gate 的连接，不需要主动连 Gate 或彼此
        if (nodeManager.getNodeType() == NodeType.GATE && xNode.getNodeType() != nodeManager.getNodeType()) {
            ClusterClient clusterClient = clusterClientMap.get(xNode);
            if (clusterClient != null) {
                clusterClient.shutdown();
            }
            ConnectPool connectPool = getXConnectPool(xNode.getNodeConfig().getTcpAddress());
            clusterClientMap.put(xNode, new ClusterClient(xNode, connectPool));
        }

        // 网关节点注册微服务
        if (nodeManager.getNodeType() == NodeType.GATE) {
            // 增加微服务索引
            Set<Integer> micMessageTypes = xNode.getNodeConfig().getMicServiceMessageTypes();
            if (micMessageTypes != null && !micMessageTypes.isEmpty()) {
                micMessageTypes.forEach(messageType -> {
                    List<XNode> xNodes = micServiceIndexs.computeIfAbsent(messageType, m -> new ArrayList<>());
                    if (!xNodes.contains(xNode)) {
                        xNodes.add(xNode);
                    }
                });
            }
        }
    }

    /**
     * 当节点被移除时
     *
     * @param xNode
     */
    private void nodeRemove(XNode xNode) {
        // leader不进行处理
        if (xNode.getNodeConfig() != null && !StringUtils.isEmpty(xNode.getNodeConfig().getType())) {
            NodeType nodeType = NodeType.valueOf(xNode.getNodeConfig().getType());
            if (nodeType == NodeType.LEADER) {
                log.debug("nodeRemove leader类型不进行处理!nodeConfig:{}", JSONObject.toJSONString(xNode.getNodeConfig()));
                return;
            }
        }
        ClusterClient clusterClient = clusterClientMap.remove(xNode);
        if (clusterClient != null) {
            clusterClient.shutdown();
        }
        // 删除微服务索引
        Set<Integer> micMessageTypes = xNode.getNodeConfig().getMicServiceMessageTypes();
        if (micMessageTypes != null && !micMessageTypes.isEmpty()) {
            micMessageTypes.forEach(messageType -> {
                List<XNode> xNodes = micServiceIndexs.get(messageType);
                if (xNodes != null && xNodes.contains(xNode)) {
                    xNodes.remove(xNode);
                    if (xNodes.isEmpty()) {
                        micServiceIndexs.remove(messageType);
                    }
                }
            });
        }
    }

    /**
     * 微服务消息分配
     *
     * @param currentClient
     * @param messageType
     * @return
     */
    public Connect micServiceAllot(ClusterClient currentClient, int messageType) {
        List<XNode> micServiceNodes = micServiceIndexs.get(messageType);
        if (micServiceNodes == null || micServiceNodes.isEmpty()) {
            return null;
        }
        // 如果当前用户所在连接，可以处理该微服务消息，则不选择其他连接
        if (micServiceNodes.contains(currentClient.xNode)) {
            return null;
        }
        // 权重为0的节点不处理 微服务消息
        List<XNode> canUseMicServiceNodes = micServiceNodes.stream().filter(xNode -> {
            NodeConfig nodeConfig = xNode.getNodeConfig();
            return nodeConfig != null && nodeConfig.getWeight() > 0;
        }).collect(Collectors.toList());
        if (canUseMicServiceNodes.isEmpty()) {
            return null;
        }
        int index = (int) (Math.random() * canUseMicServiceNodes.size());
        XNode xNode = canUseMicServiceNodes.get(index);
        ClusterClient clusterClient = clusterClientMap.get(xNode);
        if (clusterClient != null) {
            return clusterClient.getConnect();
        }
        return null;
    }

    @Override
    public void nodeChange(NodeChangeType nodeChangeType, XNode xNode) {
        log.debug("集群节点信息修改,nodePath={}", xNode.getNodePath());
        switch (nodeChangeType) {
            case NODE_ADD:
                nodeAdd(xNode);
                break;
            case NODE_REMOVE:
                nodeRemove(xNode);
                break;
        }
    }

    private static final AtomicBoolean created = new AtomicBoolean(false);

    private void writePidFile() {
        if (created.compareAndSet(false, true)) {
            try {
                File pidFile = new File("PID");
                log.info("输出 PID 文件，file=" + pidFile.getAbsolutePath());
                new ApplicationPid().write(pidFile);
                pidFile.deleteOnExit();
            } catch (Exception ex) {
                String message = String.format("Cannot create pid file %s",
                        "PID");
                log.warn(message, ex);
            }
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        writePidFile();
    }

    @Override
    public void onTimer(TimerEvent e) {
        if ("ClusterSystem".equals(e.getParameter())) {
            if (nodeManager.getNodeType() != NodeType.GATE) {
                log.info("节点信息,节点权重={}，当前session数量={}", localNodeConfig.getWeight(), sessionMap.size());
            }
            if (localNodeConfig.getWeight() == 0 && sessionMap.isEmpty()) {
                // System.exit(0);
            }
        }
    }
}
