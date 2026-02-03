package de.kseek.core.cluster;

import lombok.Getter;
import de.kseek.core.config.NodeConfig;
import de.kseek.core.curator.XNode;
import de.kseek.core.net.Connect;
import de.kseek.core.netty.ConnectPool;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class ClusterClient {
    /**
     * 远程节点的配置信息（来自其他节点）
     */
    @Getter
    private NodeConfig remoteNodeConfig;
    /**
     * 节点信息
     */
    public XNode xNode;
    /**
     * 连接池
     */
    public ConnectPool connectPool;

    public ClusterClient(XNode xNode, ConnectPool connectPool) {
        this.xNode = xNode;
        this.remoteNodeConfig = xNode.getNodeConfig();
        this.connectPool = connectPool;
    }

    public Connect getConnect() {
        return connectPool.getConnect();
    }

    public Connect getConnectSync() throws InterruptedException {
        return connectPool.getConnectSync();
    }

    public void write(Object msg) throws InterruptedException {
        connectPool.getConnect().write(msg);
    }

    public boolean canReceive(int messageType) {
        if (remoteNodeConfig != null && remoteNodeConfig.getMicServiceMessageTypes() != null) {
            return remoteNodeConfig.getMicServiceMessageTypes().contains(messageType);
        }
        return false;
    }

    public String getType() {
        return remoteNodeConfig != null ? remoteNodeConfig.getType() : null;
    }

    public void close(Connect connect) {
        connectPool.close(connect);
        if (connect != null) {
            connect.close();
        }
    }

    public void shutdown() {
        if (connectPool != null) {
            connectPool.shutdown();
        }
    }
}
