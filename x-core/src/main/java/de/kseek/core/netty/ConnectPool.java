package de.kseek.core.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import de.kseek.core.net.Connect;
import de.kseek.core.net.ConnectListener;
import de.kseek.core.net.NetAddress;
import de.kseek.core.timer.TimerCenter;
import de.kseek.core.timer.TimerEvent;
import de.kseek.core.timer.TimerListener;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Slf4j
public class ConnectPool implements ConnectListener, TimerListener, ChannelFutureListener {
    private final static int MAX_POOL_SIZE = 20;
    private final static int POOL_SIZE = 10;

    private final NetAddress netAddress;

    private final NetAddress localAddress;

    private ChannelInitializer<SocketChannel> initializer;

    private int poolSize;

    private Bootstrap bootstrap;

    private List<NettyConnect> connectList;

    private final Random random = new Random();

    private TimerCenter timerCenter;

    public ConnectPool(NetAddress netAddress, ChannelInitializer<SocketChannel> initializer) {
        this(netAddress, null, initializer, POOL_SIZE);
    }

    public ConnectPool(NetAddress netAddress, ChannelInitializer<SocketChannel> initializer, int poolSize) {
        this(netAddress, null, initializer, poolSize);
    }

    public ConnectPool(NetAddress netAddress, NetAddress localAddress, ChannelInitializer<SocketChannel> initializer) {
        this(netAddress, localAddress, initializer, POOL_SIZE);
    }

    public ConnectPool(NetAddress netAddress, NetAddress localAddress, ChannelInitializer<SocketChannel> initializer,
                       int poolSize) {
        this.netAddress = netAddress;
        this.initializer = initializer;
        this.poolSize = poolSize > 0 ? poolSize : POOL_SIZE;
        this.localAddress = localAddress;
    }

    public ConnectPool init() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
                .handler(initializer);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        if (localAddress != null) {
            bootstrap.localAddress(localAddress.getHost(), localAddress.getPort());
        }
        bootstrap.remoteAddress(netAddress.getHost(), netAddress.getPort());
        connectList = new CopyOnWriteArrayList<>();
        return this;
    }

    public ConnectPool start(TimerCenter timerCenter) {
        log.debug("start net={}", netAddress);
        //create();
        if (timerCenter != null) {
            this.timerCenter = timerCenter;
            timerCenter.add(new TimerEvent<>(this, "", 30, Integer.MAX_VALUE, 5).withTimeUnit(TimeUnit.SECONDS));
        }
        return this;
    }

    public void addConnect(NettyConnect connect) {
        connectList.add(connect);
        connect.addConnectListener(this);
    }

    public NettyConnect getConnect() {
        if (connectList.isEmpty()) {
            create();
            return null;
        }
        if (connectList.size() == 1) {
            return connectList.get(0);
        }
        int index = random.nextInt(connectList.size());
        return connectList.get(index);
    }

    /**
     * 同步获取连接
     *
     * @return
     * @throws InterruptedException
     */
    public NettyConnect getConnectSync() throws InterruptedException {
        NettyConnect c = getConnect();
        if (c == null) {
            c = (bootstrap.connect().sync().channel().pipeline().get(NettyConnect.class));
            connectList.add(c);
        }
        return c;
    }

    private void create() {
        log.debug("开始创建连接,address={}", netAddress);
        bootstrap.connect().addListener(this);
    }

    public void close(Connect connect) {
        connectList.remove(connect);
    }

    @Override
    public void onConnectClose(Connect connect) {
        connectList.remove(connect);
    }

    @Override
    public void onTimer(TimerEvent e) {
        if (connectList.size() < POOL_SIZE) {
            create();
        }
//        Ping ping = new Ping();
//        PFMessage pfMessage = new PFMessage(1, 1, ProtostuffUtil.serialize(ping));
//        ClusterMessage clusterMessage = new ClusterMessage(pfMessage);
//        for (NettyConnect connect : connectList) {
//            if (connect != null) {
//                connect.write(clusterMessage);
//            }
//        }
    }

    @Override
    public void operationComplete(ChannelFuture cf) {
        if (cf.isSuccess()) {
            NettyConnect connect = cf.channel().pipeline().get(NettyConnect.class);
            connect.addConnectListener(this);
        } else {
            if (cf.channel() != null) {
                cf.channel().close();
            }
            log.warn("连接创建失败", cf.cause());
        }
    }

    public void shutdown() {
        log.info("连接池关闭");
        if (timerCenter != null) {
            timerCenter.remove(this);
            for (Connect connect : connectList) {
                try {
                    connect.close();
                } catch (Exception e) {
                    log.warn("连接池关闭连接异常", e);
                }
            }
            connectList.clear();
        }
    }
}
