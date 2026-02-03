package de.kseek.core.curator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.NetUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import de.kseek.core.config.NodeConfig;
import de.kseek.core.config.ZookeeperConfig;
import de.kseek.core.constant.XConstant;
import de.kseek.core.micservice.MicServiceManager;
import de.kseek.core.monitor.FileLoader;
import de.kseek.core.monitor.FileMonitor;
import de.kseek.core.net.NetAddress;
import de.kseek.core.util.FileHelper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * 业务层，管理当前节点的生命周期和配置
 * @author kseek
 * @date 2024/3/22
 */
@Component
@Order(4)
@Slf4j
@RequiredArgsConstructor
public class NodeManager implements ApplicationRunner, XCuratorListener, XNodeListener, FileLoader {
    private static final String CONFIG_FILE_PATH = "config/nodeConfig.json";

    /**
     * 本节点的配置信息
     */
    private final NodeConfig localNodeConfig;
    private final XCurator xCurator;
    private final ZookeeperConfig zkConfig;
    private final FileMonitor fileMonitor;
    private final MicServiceManager micServiceManager;

    public String nodePath;
    private boolean init;

    public NodeType getNodeType() {
        return NodeType.valueOf(localNodeConfig.getType());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("nodeManager start");
        randomNodeConfig();
        obConfig();
        readArgs(args);
        init = true;
        // 初始化阶段只完成配置与文件监听，真正的注册统一在 xCuratorRefreshed 回调中完成，
        // 避免与 Curator 初始化完成时的回调并发调用 register() 导致 ZK 节点创建冲突
    }

    @Override
    public void xCuratorRefreshed(XCurator xCurator) {
        // 如果 NodeManager 还未初始化完成，延迟注册
        // 由于 CuratorCache.start() 是异步的，initialized() 回调可能在 NodeManager.run() 之前执行
        if (!init) {
            log.debug("NodeManager 尚未初始化完成，等待初始化后再注册");
            // 使用新线程延迟执行，确保 NodeManager.run() 先完成
            new Thread(() -> {
                try {
                    // 最多等待 5 秒，每 100ms 检查一次
                    for (int i = 0; i < 50 && !init; i++) {
                        Thread.sleep(100);
                    }
                    register();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("等待 NodeManager 初始化被中断", e);
                }
            }).start();
            return;
        }
        register();
    }


    private void readArgs(ApplicationArguments args) {
        List<String> numberList = args.getOptionValues("cluster.number");
        List<String> nameList = args.getOptionValues("cluster.name");
        List<String> wsAddressList = args.getOptionValues("cluster.wss-str-address");
        List<String> portList = args.getOptionValues("cluster.port");
        List<String> gameTypes = args.getOptionValues("cluster.gameTypes");
        List<String> whiteIpList = args.getOptionValues("cluster.whiteIpList");
        List<String> whiteIdList = args.getOptionValues("cluster.whiteIdList");
        List<String> weightList = args.getOptionValues("cluster.weight");
        if (CollUtil.isNotEmpty(nameList)) {
            localNodeConfig.setName(nameList.get(0));
        }
        if (CollUtil.isNotEmpty(nameList)) {
            localNodeConfig.setNumber(Integer.parseInt(numberList.get(0)));
        }
        if (CollUtil.isNotEmpty(wsAddressList)) {
            localNodeConfig.setWssStrAddress(wsAddressList.get(0));
        }
        if (CollUtil.isNotEmpty(portList)) {
            if (localNodeConfig.getTcpAddress() == null) {
                localNodeConfig.setTcpAddress(new NetAddress());
            }
            localNodeConfig.getTcpAddress().setHost(NetUtil.getLocalhostStr());
            localNodeConfig.getTcpAddress().setPort(Integer.parseInt(portList.get(0)));
        }
        if (CollUtil.isNotEmpty(gameTypes)) {
            int[] gts = new int[gameTypes.size()];
            for (int i = 0; i < gameTypes.size(); i++) {
                gts[i] = Integer.parseInt(gameTypes.get(i));
            }
            localNodeConfig.setGameTypes(gts);
        }
        if (CollUtil.isNotEmpty(whiteIdList)) {
            int[] whiteIds = new int[whiteIdList.size()];
            for (int i = 0; i < whiteIdList.size(); i++) {
                whiteIds[i] = Integer.parseInt(whiteIdList.get(i));
            }
            localNodeConfig.setWhiteIdList(whiteIds);
        }
        if (CollUtil.isNotEmpty(whiteIpList)) {
            String[] whiteIps = new String[whiteIpList.size()];
            for (int i = 0; i < whiteIpList.size(); i++) {
                whiteIps[i] = whiteIpList.get(i);
            }
            localNodeConfig.setWhiteIpList(whiteIps);
        }
        if (CollUtil.isNotEmpty(weightList)) {
            localNodeConfig.setWeight(Integer.parseInt(weightList.get(0)));
        }
    }

    public void randomNodeConfig() {
        // number 仅在 name 为空时参与默认 name 的生成；name 决定 ZK 节点路径，若撞车会导致注册冲突
        int number = (int) (Math.random() * 999 + 1);
        if (localNodeConfig.getNumber() <= 0) {
            localNodeConfig.setNumber(number);
        }
        if (StringUtils.isBlank(localNodeConfig.getName())) {
            // 避免多实例同时启动时随机 number 撞车导致 ZK path 冲突（例如 /{xRoot}/cluster/TYPE/{name}）
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            localNodeConfig.setName(localNodeConfig.getType() + "-" + localNodeConfig.getNumber() + "-" + suffix);
        }
        NetAddress netAddress = localNodeConfig.getTcpAddress();
        if (netAddress != null && StringUtils.isBlank(netAddress.getHost())) {
            netAddress.setHost(NetUtil.getLocalhostStr());
        }
    }

    /**
     * 监听配置文件
     */
    public void obConfig() {
        fileMonitor.addFileObserver(CONFIG_FILE_PATH, this, true);
    }

    @Override
    public void load(File file, boolean isNew) {
        try {
            log.info("on file change filename = {}，isNew={}", file.getName(), isNew);
            readConfig(file);
        } catch (Exception e) {
            log.warn("on file change err,filename = " + file.getName() + "，isNew=" + isNew, e);
        }

    }

    public void readConfig(File file) {
        String content = FileHelper.readFile(file, "UTF-8");
        JSONObject jsonObject = JSON.parseObject(content);
        if (jsonObject == null) {
            log.warn("节点配置文件解析错误,fileName={}", file.getName());
            return;
        }
        if (jsonObject.containsKey("weight")) {
            int weight = jsonObject.getIntValue("weight");
            localNodeConfig.setWeight(weight);
        }
        if (jsonObject.containsKey("whiteIpList")) {
            JSONArray whiteIpArray = jsonObject.getJSONArray("whiteIpList");
            if (whiteIpArray != null) {
                localNodeConfig.setWhiteIpList(whiteIpArray.toArray(new String[0]));
            }
        }
        if (jsonObject.containsKey("whiteIdList")) {
            JSONArray whiteIdArray = jsonObject.getJSONArray("whiteIdList");
            if (whiteIdArray != null) {
                int[] ids = new int[whiteIdArray.size()];
                for (int i = 0; i < ids.length; i++) {
                    ids[i] = whiteIdArray.getIntValue(i);
                }
                localNodeConfig.setWhiteIdList(ids);
            }
        }
        if (jsonObject.containsKey("workPoolNum")) {
            int workPoolNum = jsonObject.getIntValue("workPoolNum");
            localNodeConfig.setWorkPoolNum(workPoolNum);
        }
        update();
    }

    public String generateNodePath(NodeConfig nodeConfig) {
        return XConstant.SEPARATOR + nodeConfig.getParentPath() + XConstant.SEPARATOR +
                nodeConfig.getType() + XConstant.SEPARATOR +
                nodeConfig.getName();
    }

    private String createPathData() {
        String path = generateNodePath(localNodeConfig);
        //添加微服务
        if (localNodeConfig.isShowMicService()) {
            localNodeConfig.setMicServiceMessageTypes(micServiceManager.messageTypes);
        } else {
            localNodeConfig.setMicServiceMessageTypes(null);
        }
        return path;
    }

    private String getLocalHost() {
        try {
            String host = NetUtil.getLocalhostStr();
            log.info("获取到本地IP地址，Host:{}", host);
            return host;
        } catch (Exception e) {
            log.warn("获取本地IP地址错误", e);
        }
        return null;
    }

    private String getNodeConfigJson() {
        if (StringUtils.isBlank(localNodeConfig.getTcpAddress().getHost())) {
            localNodeConfig.getTcpAddress().setHost(getLocalHost());
        }
        return JSON.toJSONString(localNodeConfig, JSONWriter.Feature.PrettyFormat);
    }

    private void register() {
        try {
            if (!init) {
                log.info("nodeConfig 节点数据未初始化完成");
                return;
            }
            String path = createPathData();
            log.info("node register,path is {}", path);

            String nodeConfigJson = getNodeConfigJson();
            nodePath = xCurator.addPath(path, nodeConfigJson.getBytes(StandardCharsets.UTF_8), false);
            // addPath 内部会拼接 rootPath，返回的 nodePath 才是 CuratorCache 事件里出现的真实路径
            // 如果用 path（不带 root）去监听，会导致 NODE_REMOVE 等事件无法匹配到监听器
//            xCurator.addXNodeListener(path, this);
            xCurator.addXNodeListener(nodePath, this);
        } catch (Exception e) {
            log.warn("node register fail.", e);
        }
    }

    public void updateFormNodeConfig(NodeConfig config) {
        try {
            if (config == null) {
                log.warn("node update fail,参数错误");
                return;
            }
            String nodePath = generateNodePath(config);
            log.info("node updateFormNodeConfig,path is {}", nodePath);
            String configData = JSON.toJSONString(config, JSONWriter.Feature.PrettyFormat);
            xCurator.updatePath(nodePath, configData.getBytes(StandardCharsets.UTF_8), false);
        } catch (Exception e) {
            log.warn("node update fail.", e);
        }
    }

    public void update() {
        try {
            if (!init) {
                log.info("节点数据未初始化完成");
                return;
            }
            String path = createPathData();
            log.info("node update,path is {}", path);
            String nc = getNodeConfigJson();
            nodePath = xCurator.updatePath(path, nc.getBytes(StandardCharsets.UTF_8), false);
        } catch (Exception e) {
            log.warn("node update fail.", e);
        }
    }

    public XNode getXNode(NodeType nodeType) {
        String nodeTypePath = XConstant.SEPARATOR + zkConfig.getXRoot() + XConstant.SEPARATOR + localNodeConfig.getParentPath()
                + XConstant.SEPARATOR + nodeType;
        return xCurator.getXNode(nodeTypePath);
    }

    public XNode getXNode(String nodeType) {
        String nodePath = XConstant.SEPARATOR + zkConfig.getXRoot() + XConstant.SEPARATOR + localNodeConfig.getParentPath()
                + XConstant.SEPARATOR + nodeType;
        return xCurator.getXNode(nodePath);
    }

    public String getXNodePath(String nodeType, String nodeName) {
        return XConstant.SEPARATOR + zkConfig.getXRoot() + XConstant.SEPARATOR + localNodeConfig.getParentPath()
                + XConstant.SEPARATOR + nodeType + XConstant.SEPARATOR + nodeName;
    }

    @Override
    public void nodeChange(NodeChangeType nodeChangeType, XNode xNode) {
        switch (nodeChangeType) {
            case NODE_ADD:

                break;
            case NODE_REMOVE:
                log.warn("本节点被异常移除");
                register();
                break;
            case DATA_CHANGE:
                // 当前仅在节点数据发生变化时可能用到；本节点配置热更新由本地文件监听触发 update()
                break;
        }
    }
}
