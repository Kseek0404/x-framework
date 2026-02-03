package de.kseek.core.config;

import lombok.Data;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Data
public class ServerConfig {
    /**
     * 服务器ID
     */
    private int serverId;
    /**
     * 服务器类型
     */
    private String serverType;
    /**
     * 服务器名称
     */
    private String serverName;
    /**
     * 系统扫描包
     */
    private String[] scanPackages;
}
