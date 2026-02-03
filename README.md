# x-framework

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.13-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Netty](https://img.shields.io/badge/Netty-4.1.94-blue.svg)](https://netty.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[English](README.en.md) | 中文

基于 Spring Boot 与 Netty 的**游戏 / 分布式服务框架**，提供集群通信、网关会话、Protostuff 消息、Zookeeper 服务发现与 WebSocket 等能力，适合作为多节点游戏服务器或高并发长连接服务的基础设施。

---

## 特性

- **集群通信**：基于 Netty 的节点间消息收发、会话路由与集群消息分发
- **网关层**：TCP/WebSocket 网关，支持 WSS、会话验证、踢线、跨节点转发
- **消息协议**：Protostuff 序列化 + 注解驱动的消息路由（类似 RPC 调用）
- **服务发现**：Apache Curator 集成 Zookeeper，节点注册、发现与监听
- **定时与调度**：内置 Timer 与 Quartz Scheduler 支持
- **微服务抽象**：MicService 接口与管理器，便于扩展业务服务
- **文件监控**：配置/资源文件变更监听与热加载

---

## 技术栈

| 类别     | 技术 |
|----------|------|
| 基础框架 | Spring Boot 2.7.13 |
| 网络通信 | Netty 4.1.94 |
| 序列化   | Protostuff、Protobuf、Jackson、Fastjson |
| 服务发现 | Apache Curator 5.5.0、Zookeeper |
| 调度     | Quartz |
| 工具     | Hutool、Lombok、CGLib、ReflectASM |

---

## 项目结构

```
x-framework/
├── pom.xml                 # 父 POM，统一版本与依赖
├── x-core/                 # 核心库：集群、网关、消息、Curator 等
│   └── src/main/java/de/kseek/core/
│       ├── cluster/        # 集群连接、消息编解码与分发
│       ├── config/         # 节点、服务器、Zookeeper 配置
│       ├── curator/        # Zookeeper 节点管理与监听
│       ├── gate/           # 网关配置、编解码、会话与集群消息转发
│       ├── listener/       # 会话建立、关闭、验证、登出等监听
│       ├── message/        # 集群消息体（注册、会话创建/踢线/登出等）
│       ├── net/            # 连接、会话、收件箱抽象
│       ├── netty/          # Netty 服务端与连接池
│       ├── protostuff/     # Protostuff 消息管理与控制器
│       ├── timer/          # 定时器与调度中心
│       ├── ws/             # WebSocket 处理与 WSS
│       └── ...
├── x-java2pb/              # Java 转 Protobuf 等代码生成工具
└── x-framework/            # 应用入口模块，依赖 x-core、x-java2pb
    └── src/main/java/de/kseek/Main.java
```

---

## 环境要求

- **JDK**：1.8+
- **Maven**：3.6+（可选：项目自带 Maven Wrapper，可直接使用 `./mvnw` 或 `mvnw.cmd`，无需单独安装 Maven）
- **Zookeeper**：若使用集群与 Curator，需提前部署（可选）

---

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/Kseek0404/x-framework.git
cd x-framework
```

### 2. 编译

推荐使用项目自带的 Maven Wrapper（无需预先安装 Maven）：

```bash
# Linux / macOS
./mvnw clean install -DskipTests

# Windows (PowerShell 或 CMD)
.\mvnw.cmd clean install -DskipTests
```

若已安装 Maven，也可使用：`mvn clean install -DskipTests`

### 3. 运行示例

```bash
cd x-framework
./mvnw spring-boot:run
```

Windows 下使用：`mvnw.cmd spring-boot:run`

当前 `x-framework` 模块的 `Main` 为占位入口，实际使用时需在业务模块中引入 `x-core`，配置网关、集群与 Zookeeper 等，并实现 `ClusterService`、会话监听与 Protostuff 消息处理。

### 4. 作为依赖使用

在业务项目中引入核心库：

```xml
<dependency>
    <groupId>de.kseek</groupId>
    <artifactId>x-core</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

---

## 核心概念简述

- **Gate**：对外提供 TCP/WebSocket 连接，处理客户端上行并转发到集群内其他节点。
- **Cluster**：节点间通过 Netty 长连接通信，支持会话绑定、消息分发与踢线等。
- **Curator / XCurator**：连接 Zookeeper，注册本节点、发现其他节点并监听上下线。
- **Protostuff 消息**：通过注解将请求路由到对应 Controller 方法，实现类似 RPC 的调用。

---

## 配置说明

主要配置来源于 Spring 配置与以下类对应的属性（具体字段请参考源码）：

- **Zookeeper**：`ZookeeperConfig`（连接串、根路径等）
- **节点**：`NodeConfig`（节点类型、ID 等）
- **服务器**：`ServerConfig`
- **网关**：`GateConfig`（TCP/WS 地址、是否 WSS、SSL 路径等）

在 `application.yml` 或 `application.properties` 中按需配置上述项即可。

---

## 参与贡献

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/xxx`
3. 提交改动：`git commit -m 'feat: add xxx'`
4. 推送到分支：`git push origin feature/xxx`
5. 提交 Pull Request

---

## 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 相关链接

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Netty](https://netty.io/)
- [Apache Curator](https://curator.apache.org/)
- [Protostuff](https://github.com/protostuff/protostuff)

如有问题或建议，欢迎提 Issue 或 Pull Request。
