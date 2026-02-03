package de.kseek.core.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import de.kseek.core.net.NetAddress;

import java.util.Set;

/**
 * @author kseek
 * @date 2024/3/22
 */
@ConfigurationProperties(prefix = "x.cluster")
@Component
@Setter
@Getter
@Order(1)
public class NodeConfig {

    /**
     * 节点默认父目录
     */
    private String parentPath = "cluster";
    /**
     * 节点类型
     */
    private String type;
    /**
     * 节点名称
     */
    private String name;
    /**
     * 节点编号 启动的时候 随机生成的
     */
    private int number;
    /**
     * 是否使用网关
     */
    private boolean useGate;
    /**
     * 用于前端建立连接的地址
     */
    private String wssStrAddress;
    /**
     * 用于前端建立连接的地址
     */
    private String tcpStrAddress;
    /**
     * 节点tcp服务地址
     */
    private NetAddress tcpAddress;
    /**
     * 节点web服务地址
     */
    private NetAddress httpAddress;
    /**
     * 节点rpc服务地址
     */
    private NetAddress rpcAddress;
    /**
     * 该节点接收的消息类型
     */
    private int[] messageTypes;
    /**
     * 该节点支持的游戏类型
     */
    private int[] gameTypes;
    /**
     * 节点权重 必须得 >0 才可用 否则视为不可用
     * 即使使用白名单 ip 或者 id  权重也得>0
     */
    private int weight = 1;
    /**
     * IP白名单
     */
    private String[] whiteIpList;
    /**
     * 用户ID白名单
     */
    private int[] whiteIdList;
    /**
     * 单连接是否使用工作线程池
     */
    private boolean workPool;
    /**
     * 业务线程数
     */
    private int workPoolNum = Runtime.getRuntime().availableProcessors() * 16;
    /**
     * 节点支持的微服务消息号
     */
    private Set<Integer> micServiceMessageTypes;
    /**
     * 是否暴露微服务
     */
    private boolean showMicService = true;
    /**
     * 节点配置版本号
     */
    private String configVersion;
    /**
     * 集群间连接数量
     */
    private int clusterConnectPoolSize = 6;
    /**
     * 扫描消息包
     */
    public String[] messagePkgs;
}
