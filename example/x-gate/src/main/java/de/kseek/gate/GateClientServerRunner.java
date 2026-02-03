package de.kseek.gate;

import de.kseek.core.gate.GateChannelInitializer;
import de.kseek.core.netty.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 网关客户端入口：启动对外 TCP 服务，供游戏客户端连接
 */
@Slf4j
@Component
@Order(10)
public class GateClientServerRunner implements ApplicationRunner {

    @Value("${x.gate.client-port:9000}")
    private int clientPort;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        NettyServer clientServer = new NettyServer(clientPort, new GateChannelInitializer(30));
        clientServer.setName("gate-client-" + clientPort);
        clientServer.start();
        log.info("Gate 客户端入口已启动，端口: {}", clientPort);
    }
}
