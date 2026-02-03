package de.kseek.core.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import de.kseek.core.net.Connect;
import de.kseek.core.net.ConnectListener;
import de.kseek.core.net.NetAddress;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Slf4j
public abstract class NettyConnect extends SimpleChannelInboundHandler<Object> implements Connect {
    protected Channel channel;
    protected NetAddress remoteAddress;
    protected List<ConnectListener> connectListeners = new CopyOnWriteArrayList<>();
    protected long lastHeartbeatTime;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channel = ctx.channel();
        log.debug("连接创建完成,ctx={}", ctx);
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        remoteAddress = new NetAddress(address.getAddress().getHostAddress(), address.getPort());
        onCreate();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.debug("tips caught exception,ctx={}", ctx, cause);
        close();
    }

    @Override
    public boolean write(Object msg) {
        try {
            channel.writeAndFlush(msg).addListener(future -> {
                if (!future.isSuccess()) {
                    Throwable e = future.cause();
                    if (e != null) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    @Override
    public NetAddress address() {
        return remoteAddress;
    }

    @Override
    public void close() {
        log.debug("服务器主动关闭连接,netAddress={},ctx={}", remoteAddress, channel);
        try {
            channel.close();
        } catch (Exception e) {
            log.warn("关闭连接异常,netAddress={},ctx={}", remoteAddress, channel, e);
        }

    }


    public void writeAndClose(Object obj) {
        log.debug("服务器主动关闭连接并通知,netAddress={},ctx={}", remoteAddress, channel);
        try {
            channel.writeAndFlush(obj).addListener(future -> {
                if (isActive()){
                    channel.close();
                }
            });
        } catch (Exception e) {
            log.warn("关闭连接异常,netAddress={},ctx={}", remoteAddress, channel, e);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        messageReceived(msg);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel inactive,ctx={}", ctx);
        connectListeners.forEach(connectListener -> connectListener.onConnectClose(this));
        connectListeners.clear();
        onClose();
        super.channelInactive(ctx);
    }

    /**
     * 当消息到达
     *
     * @param msg
     */
    public abstract void messageReceived(Object msg);

    @Override
    public void addConnectListener(ConnectListener connectListener) {
        connectListeners.add(connectListener);
    }

    @Override
    public void removeConnectListener(ConnectListener connectListener) {
        connectListeners.remove(connectListener);
    }

    @Override
    public String toString() {
        return "NettyConnect{" +
                "ctx=" + channel +
                ", remoteAddress=" + remoteAddress +
                '}';
    }
}
