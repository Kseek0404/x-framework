# x-framework

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.13-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Netty](https://img.shields.io/badge/Netty-4.1.94-blue.svg)](https://netty.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

English | [中文](README.md)

A **game / distributed service framework** built on Spring Boot and Netty. It provides cluster communication, gate session management, Protostuff messaging, Zookeeper-based service discovery, and WebSocket support, suitable as infrastructure for multi-node game servers or high-concurrency long-connection services.

---

## Features

- **Cluster communication**: Netty-based inter-node messaging, session routing, and cluster message dispatching
- **Gate layer**: TCP/WebSocket gate with WSS, session verification, kick-out, and cross-node forwarding
- **Message protocol**: Protostuff serialization and annotation-driven message routing (RPC-style)
- **Service discovery**: Apache Curator with Zookeeper for node registration, discovery, and watchers
- **Scheduling**: Built-in Timer and Quartz Scheduler support
- **Micro-service abstraction**: MicService interface and manager for extending business services
- **File monitoring**: Config/resource file change listeners and hot-reload

---

## Tech Stack

| Category   | Technologies |
|-----------|--------------|
| Base      | Spring Boot 2.7.13 |
| Network   | Netty 4.1.94 |
| Serialization | Protostuff, Protobuf, Jackson, Fastjson |
| Discovery | Apache Curator 5.5.0, Zookeeper |
| Scheduling | Quartz |
| Utilities | Hutool, Lombok, CGLib, ReflectASM |

---

## Project Structure

```
x-framework/
├── pom.xml                 # Parent POM
├── x-core/                 # Core: cluster, gate, messaging, Curator, etc.
│   └── src/main/java/de/kseek/core/
│       ├── cluster/        # Cluster connection, codec, dispatcher
│       ├── config/         # Node, server, Zookeeper config
│       ├── curator/        # Zookeeper node management and listeners
│       ├── gate/           # Gate config, codec, session, cluster forwarding
│       ├── listener/       # Session create, close, verify, logout
│       ├── message/        # Cluster message types (register, session create/kick/logout)
│       ├── net/            # Connect, session, inbox abstractions
│       ├── netty/          # Netty server and connection pool
│       ├── protostuff/     # Protostuff message manager and controller
│       ├── timer/          # Timer and scheduler center
│       ├── ws/             # WebSocket and WSS handlers
│       └── ...
├── x-java2pb/              # Java-to-Protobuf code generation
└── x-framework/            # Application entry, depends on x-core, x-java2pb
    └── src/main/java/de/kseek/Main.java
```

---

## Requirements

- **JDK**: 1.8+
- **Maven**: 3.6+ (optional: use the bundled Maven Wrapper with `./mvnw` or `mvnw.cmd` so you don’t need to install Maven)
- **Zookeeper**: Required only if using cluster and Curator (optional)

---

## Quick Start

### 1. Clone

```bash
git clone https://github.com/Kseek0404/x-framework.git
cd x-framework
```

### 2. Build

Prefer the bundled Maven Wrapper (no Maven install required):

```bash
# Linux / macOS
./mvnw clean install -DskipTests

# Windows (PowerShell or CMD)
.\mvnw.cmd clean install -DskipTests
```

If Maven is already installed: `mvn clean install -DskipTests`

### 3. Run sample

```bash
cd x-framework
./mvnw spring-boot:run
```

On Windows use: `mvnw.cmd spring-boot:run`

The `x-framework` module’s `Main` is a placeholder. In real use, add `x-core` to your application, configure gate, cluster, and Zookeeper, and implement `ClusterService`, session listeners, and Protostuff message handlers.

### 4. Use as dependency

```xml
<dependency>
    <groupId>de.kseek</groupId>
    <artifactId>x-core</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

---

## Core Concepts

- **Gate**: Exposes TCP/WebSocket to clients and forwards traffic into the cluster.
- **Cluster**: Inter-node Netty connections for session binding, message dispatch, and kick-out.
- **Curator / XCurator**: Zookeeper client for node registration, discovery, and lifecycle events.
- **Protostuff messages**: Annotation-based routing to controller methods (RPC-style).

---

## Configuration

Configuration is driven by Spring and the following classes (see source for fields):

- **Zookeeper**: `ZookeeperConfig`
- **Node**: `NodeConfig`
- **Server**: `ServerConfig`
- **Gate**: `GateConfig` (TCP/WS address, WSS, SSL paths)

Configure these in `application.yml` or `application.properties` as needed.

---

## Contributing

1. Fork the repository
2. Create a branch: `git checkout -b feature/xxx`
3. Commit: `git commit -m 'feat: add xxx'`
4. Push: `git push origin feature/xxx`
5. Open a Pull Request

---

## License

This project is licensed under the [MIT License](LICENSE).

---

## Links

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Netty](https://netty.io/)
- [Apache Curator](https://curator.apache.org/)
- [Protostuff](https://github.com/protostuff/protostuff)

Issues and Pull Requests are welcome.
