package de.kseek.core.curator;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import de.kseek.core.cluster.ClusterHelper;
import de.kseek.core.config.NodeConfig;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Setter
@Getter
@Slf4j
@ToString
public class XNode {
    /**
     * 节点路径
     */
    private String nodePath;
    /**
     * 节点数据
     */
    private String nodeData;
    /* 节点信息*/
    private NodeConfig nodeConfig;
    /**
     * 子节点
     */
    private final Map<String, XNode> childrenNodes = new HashMap<>();

    private String strStat;


    public XNode(String nodePath, String nodeData, String strStat) {
        this.nodePath = nodePath;
        this.nodeData = nodeData;
        this.strStat = strStat;
    }

    public NodeType getNodeType() {
        return NodeType.valueOf(nodeConfig.getType());
    }

    public XNode addChildren(XNode xNode) {
        XNode mn = getChildren(xNode.getNodePath(), false);
        // 找不到或者是本层目录的节点，则直接更新
        if (mn == null || mn.getNodePath().equals(xNode.getNodePath())) {
            return childrenNodes.put(xNode.getNodePath(), xNode);
        } else {
            return mn.addChildren(xNode);
        }
    }

    public XNode getChildren(String path, boolean rc) {
        if (!childrenNodes.isEmpty()) {
            for (XNode xNode : childrenNodes.values()) {
                if (path.equals(xNode.getNodePath())) {
                    return xNode;
                }
                if (path.startsWith(xNode.getNodePath())) {
                    if (rc) {
                        return xNode.getChildren(path, rc);
                    }
                    return xNode;
                }
            }
        }
        return null;
    }

    public XNode removeChildren(String path) {
        if (childrenNodes.containsKey(path)) {
            return childrenNodes.remove(path);
        } else {
            XNode xNode = getChildren(path, false);
            if (xNode != null) {
                return xNode.removeChildren(path);
            }
        }
        return null;
    }

    /**
     * 获取本节点下的所有子节点
     *
     * @return
     */
    public List<XNode> getAllChildren() {
        return new ArrayList<>(childrenNodes.values());
    }

    /**
     * 从节点中随机选择一个子节点，如果节点不包含子节点返回null
     *
     * @return
     */
    public XNode randomOneXNode() {
        if (childrenNodes.isEmpty()) {
            return null;
        }
        List<XNode> childrens = getAllChildren();
        Random random = new Random();
        int p = random.nextInt(childrens.size());
        return childrens.get(p);
    }

    /**
     * 获取子节点
     * 通过ip进行hash 一致性选择，如果ip为空，随机一个节点
     *
     * @param ip
     * @return
     */
    public XNode getNode(String ip) {
        List<XNode> xNodeList = getAllChildren();
        if (xNodeList == null || xNodeList.isEmpty()) {
            return null;
        }
        int index = 0;
        int length = xNodeList.size();
        // 通过ip hash一致性
        if (StringUtils.hasText(ip)) {
            int hash = ip.hashCode();
            index = Math.abs(hash) % length;
        } else {
            index = (int) (length * Math.random());
        }
        return xNodeList.get(index);
    }

    public XNode randomOneXNodeWithWeight(String ip, long id) {
        if (childrenNodes.isEmpty()) {
            return null;
        }

        List<XNode> childrenNodeList = getAllChildren();
        List<XNode> tempNodes = new ArrayList<>();
        List<XNode> preciseList = new ArrayList<>();
        for (XNode xNode : childrenNodeList) {
            NodeConfig nodeConfig = xNode.getNodeConfig();
            if (nodeConfig == null || nodeConfig.getWeight() <= 0) {
                continue;
            }
            // 只有在没有白名单限制的情况下，才加入普通候选列表
            if ((nodeConfig.getWhiteIdList() == null || nodeConfig.getWhiteIdList().length == 0)
                    && (nodeConfig.getWhiteIpList() == null || nodeConfig.getWhiteIpList().length == 0)) {
                tempNodes.add(xNode);
                // 只有在没有白名单限制的情况下，才加入普通候选列表
            } else if (ClusterHelper.preciseInIdWhiteList(id, nodeConfig.getWhiteIdList()) || ClusterHelper.preciseInIpWhiteList(ip, nodeConfig.getWhiteIpList())) {
                preciseList.add(xNode);
            }
        }

        List<XNode> finalCandidates = preciseList.isEmpty() ? tempNodes : preciseList;
        if (finalCandidates.isEmpty()) {
            return null;
        }
        // 如果只有一个节点  直接返回 避免后续随机计算
        if (finalCandidates.size() == 1) {
            return finalCandidates.get(0);
        }
        // 计算总权重
        int totalWeight = 0;
        for (XNode node : finalCandidates) {
            totalWeight += node.getNodeConfig().getWeight();
        }

        if (totalWeight <= 0) {
            return null;
        }

        int p = ThreadLocalRandom.current().nextInt(totalWeight);

        int runningSum = 0;
        for (XNode xNode : finalCandidates) {
            runningSum += xNode.getNodeConfig().getWeight();
            if (p < runningSum) {
                return xNode;
            }
        }
        log.error("===========================");
        log.error("== 根据权重寻找节点失败");
        log.error("===========================");
        return null;
    }

    public boolean hasChildren() {
        return !childrenNodes.isEmpty();
    }

    public NodeConfig getNodeConfig() {
        if (nodeConfig == null && nodeData != null && !nodeData.isEmpty()) {
            try {
                nodeConfig = JSON.parseObject(nodeData, NodeConfig.class);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return nodeConfig;
    }

    public void updateData(String data) {
        this.nodeData = data;
        nodeConfig = null;
        getNodeConfig();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XNode)) return false;

        XNode xNode = (XNode) o;

        return nodePath.equals(xNode.nodePath);
    }

    @Override
    public int hashCode() {
        return nodePath.hashCode();
    }

}
