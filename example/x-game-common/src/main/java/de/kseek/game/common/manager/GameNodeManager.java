package de.kseek.game.common.manager;

import de.kseek.core.cluster.ClusterHelper;
import de.kseek.core.cluster.ClusterSystem;
import de.kseek.core.config.NodeConfig;
import de.kseek.core.curator.NodeType;
import de.kseek.core.curator.XNode;
import de.kseek.core.protostuff.PFSession;
import de.kseek.core.curator.NodeManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 应用层节点管理：按类型取节点、按 IP 取 Gate、按 gameType/白名单/权重选游戏节点等。
 * 依赖框架 NodeManager、ClusterSystem，不修改框架核心。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GameNodeManager {

    private final ClusterSystem clusterSystem;
    private final NodeManager nodeManager;

    /**
     * 切换到指定节点
     */
    public void switchNode(PFSession session, XNode xNode) {
        clusterSystem.switchNode(session, xNode);
    }

    public XNode getNode(String path) {
        return clusterSystem.getNode(path);
    }

    public String getNodePath() {
        return clusterSystem.getNodePath();
    }

    public boolean hasNode(NodeType nodeType) {
        XNode xNode = nodeManager.getXNode(nodeType);
        return xNode != null && xNode.hasChildren();
    }


    /**
     * 获取指定类型下按 IP 选中的子节点（如 Gate）
     */
    public XNode getGateNode(NodeType nodeType, String ip) {
        XNode xNode = nodeManager.getXNode(nodeType);
        if (xNode == null) {
            log.error("查找节点失败，nodeType={}, ip={}", nodeType, ip);
            return null;
        }
        XNode target = xNode.getNode(ip);
        if (target == null) {
            log.error("按 IP 查找子节点失败，nodeType={}, ip={}", nodeType, ip);
        }
        return target;
    }

    /**
     * 按 gameType、playerId、ip 选一个游戏节点（白名单优先，再按权重随机）
     */
    public XNode loadGameNode(NodeType nodeType, int gameType, long playerId, String ip) {
        XNode xNode = nodeManager.getXNode(nodeType);
        if (xNode == null) {
            log.warn("查找游戏节点失败，nodeType={}, gameType={}", nodeType, gameType);
            return null;
        }
        List<XNode> allChildren = xNode.getAllChildren();
        if (allChildren == null || allChildren.isEmpty()) {
            log.warn("子节点为空，nodeType={}, gameType={}", nodeType, gameType);
            return null;
        }
        List<XNode> list = new ArrayList<>();
        List<XNode> preciseList = new ArrayList<>();
        for (XNode node : allChildren) {
            NodeConfig nodeConfig = node.getNodeConfig();
            if (nodeConfig == null) {
                continue;
            }
            if (has(nodeConfig.getGameTypes(), gameType)
                    && (nodeConfig.getWhiteIdList() == null || nodeConfig.getWhiteIdList().length == 0)
                    && (nodeConfig.getWhiteIpList() == null || nodeConfig.getWhiteIpList().length == 0)) {
                list.add(node);
            } else if (has(nodeConfig.getGameTypes(), gameType)
                    && (ClusterHelper.preciseInIdWhiteList(playerId, nodeConfig.getWhiteIdList())
                    || ClusterHelper.preciseInIpWhiteList(ip, nodeConfig.getWhiteIpList()))) {
                preciseList.add(node);
            }
        }
        if (!preciseList.isEmpty()) {
            list = preciseList;
        }
        if (list.isEmpty()) {
            return null;
        }
        int totalWeight = list.stream().mapToInt(m -> m.getNodeConfig().getWeight()).sum();
        if (totalWeight <= 0) {
            return null;
        }
        Random random = new Random();
        int p = random.nextInt(totalWeight);
        int sum = 0;
        for (XNode node : list) {
            sum += node.getNodeConfig().getWeight();
            if (p < sum) {
                return node;
            }
        }
        return null;
    }

    public static boolean has(int[] arrays, int value) {
        if (arrays == null) {
            return false;
        }
        for (int v : arrays) {
            if (v == value) {
                return true;
            }
        }
        return false;
    }
}
