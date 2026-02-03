package de.kseek.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * zookeeper 相关配置
 * eg:
 *   x:
 *     zookeeper:
 *     x-root: rootName
 *     connects: 152.53.54.119:2181
 *     baseSleepTimeMs: 1000
 *     maxRetries: 3
 *     sessionTimeoutMs: 60000
 *     connectionTimeoutMs: 15000
 * @author kseek
 * @date 2024/3/22
 */
@Configuration
@ConfigurationProperties(prefix = "x.zookeeper")
@Data
public class ZookeeperConfig {
    /**
     * 根目录 区分是否归属于同一个项目
     */
    private String xRoot;
    /**
     * 连接地址配置
     */
    private String connects;
    /**
     * 连接间隔时间
     */
    private int baseSleepTimeMs;
    /**
     * 最大重试次数
     */
    private int maxRetries;
    /**
     * zk session 超时时间
     */
    private int sessionTimeoutMs = 10000;
    /**
     * zk 连接 超时时间
     */
    private int connectionTimeoutMs = 3000;
}
