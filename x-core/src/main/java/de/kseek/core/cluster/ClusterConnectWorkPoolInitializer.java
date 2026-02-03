package de.kseek.core.cluster;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class ClusterConnectWorkPoolInitializer extends ChannelInitializer<SocketChannel> {

    private static final int MSG_MAX_SIZE = 10 * 1024 * 1024;
    private static final int HEADER_SIZE = 4;
    private final ClusterMessageDispatcher clusterMessageDispatcher;
    private final String nodePath;

    public ClusterConnectWorkPoolInitializer(ClusterMessageDispatcher clusterMessageDispatcher, String nodePath) {
        this.clusterMessageDispatcher = clusterMessageDispatcher;
        this.nodePath = nodePath;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(
                        MSG_MAX_SIZE, 0, HEADER_SIZE, 0, 4))
                .addLast(new ClusterMessageDecoder())
                .addLast(new LengthFieldPrepender(HEADER_SIZE))
                .addLast(new ClusterMessageEncoder())
                .addLast("idleStateHandler", new IdleStateHandler(60, 20, 30, TimeUnit.SECONDS))
//                .addLast(new DefaultEventLoopGroup(), new ClusterConnect(clusterMessageDispatcher, nodePath));
                .addLast(new ClusterConnect(clusterMessageDispatcher, nodePath));
    }
}
