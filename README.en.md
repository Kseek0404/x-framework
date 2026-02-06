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
├── pom.xml                 # Parent POM (does not include example modules)
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
├── x-framework/            # Placeholder entry, depends on x-core, x-java2pb
│   └── src/main/java/de/kseek/Main.java
└── example/                # Example project (not part of root build; open separately)
    ├── pom.xml             # Example parent POM (x-example)
    ├── x-game-common/      # Shared: message constants, node manager, etc.
    ├── x-login/            # Login service — runnable standalone
    ├── x-gate/             # Gate service — runnable standalone
    └── x-hall/             # Hall service — runnable standalone
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

**Option A: Run the placeholder entry (x-framework module)**

```bash
cd x-framework
./mvnw spring-boot:run
```

On Windows use: `mvnw.cmd spring-boot:run`

**Option B: Run the example project (example/)**

The `example/` directory is a separate demo (login, gate, hall, etc.) and is **not** built by the root `mvn install`. Build and install the core first, then build and run from the example directory:

```bash
# 1. Install core from repo root
./mvnw clean install -DskipTests

# 2. Build example
cd example
../mvnw clean install -DskipTests

# 3. Run a service (e.g. login)
cd x-login
../mvnw spring-boot:run
```

In IDEA you can add `example/pom.xml` as a Maven project to run each module (e.g. `LoginApplication`, `GateApplication`, `HallApplication`) directly.

### 4. Use as dependency

```xml
<dependency>
    <groupId>de.kseek</groupId>
    <artifactId>x-core</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

---

## Example project (example)

The `example/` directory contains runnable demos built on x-core:

| Module         | Description |
|----------------|-------------|
| **x-game-common** | Shared layer: message constants (`GameMessageConst`), node manager (`GameNodeManager`), used by other example modules |
| **x-login**    | Login service: account login, Protostuff handlers (`LoginController`), session verification |
| **x-gate**     | Gate service: TCP/WebSocket gate, session verify listener (`GateSessionVerifyListener`), client connection entry |
| **x-hall**     | Hall service: enter hall, hall list, and related messages (`HallService`, `ReqEnterHall`, `RespHallList`, etc.) |

Ensure `mvn install` has been run at the repo root so example can resolve `x-core`. If using Zookeeper/cluster, start Zookeeper and configure each module’s `application.yml`.

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
