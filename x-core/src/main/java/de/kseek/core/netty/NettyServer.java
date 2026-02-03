package de.kseek.core.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Slf4j
public class NettyServer extends Thread {
    /**
     * 服务监听端口
     */
    private final int portNumber;
    /**
     * 服务监听地址
     */
    private final String address;
    /**
     * 连接初始化器
     */
    private ChannelInitializer<SocketChannel> initializer;

    private ServerBootstrap b;

    private int nThreads = Runtime.getRuntime().availableProcessors();

    public NettyServer(int portNumber, ChannelInitializer<SocketChannel> initializer) {
        this(null, portNumber, Runtime.getRuntime().availableProcessors(), initializer);
    }

    public NettyServer(int portNumber, int nThreads, ChannelInitializer<SocketChannel> initializer) {
        this(null, portNumber, nThreads, initializer);
    }

    public NettyServer(String address, int portNumber, int nThreads, ChannelInitializer<SocketChannel> initializer) {
        super("netty-server-" + portNumber);
        this.address = address;
        this.portNumber = portNumber;
        this.initializer = initializer;
        this.nThreads = nThreads;
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup;
        EventLoopGroup workerGroup;
        Class<? extends ServerChannel> channelClass;
        if (Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(nThreads * 2);
            channelClass = EpollServerSocketChannel.class;
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(nThreads * 2);
            channelClass = NioServerSocketChannel.class;
        }
        //workerGroup.setIoRatio(30);
        try {
            b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(channelClass);
            b.childHandler(initializer).childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 建议设置到4K
                    .option(ChannelOption.SO_BACKLOG, 4096)
                    .handler(new LoggingHandler(LogLevel.INFO));
            // 服务器绑定端口监听
            ChannelFuture f;
            if (address != null) {
                f = b.bind(address, portNumber).sync();
                log.info("Server started on address {}, port {}", address, portNumber);
            } else {
                f = b.bind(portNumber).sync();
                log.info("Server started on port {}", portNumber);
            }
            // 监听服务器关闭监听
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("\nnet server start error...\n", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
