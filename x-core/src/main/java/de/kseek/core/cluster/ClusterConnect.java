package de.kseek.core.cluster;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import de.kseek.core.message.ClusterRegisterMsg;
import de.kseek.core.netty.NettyConnect;
import de.kseek.core.protostuff.MessageUtil;
import de.kseek.core.protostuff.PFMessage;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Slf4j
public class ClusterConnect extends NettyConnect {
    /**
     * 集群消息分发器
     */
    private final ClusterMessageDispatcher clusterMessageDispatcher;
    /**
     * 节点路径
     */
    private final String nodePath;
    /**
     * ping 消息常量
     */
    private final ClusterMessage clusterPingMessage = new ClusterMessage(new PFMessage(1, 1, null));

    public ClusterConnect(ClusterMessageDispatcher clusterMessageDispatcher, String nodePath) {
        this.clusterMessageDispatcher = clusterMessageDispatcher;
        this.nodePath = nodePath;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            //长时间未收到消息
            if (e.state() == IdleState.READER_IDLE) {
                //log.debug("读取消息闲置,ctx={}" + ctx);
                //ctx.close();
                //长时间未写出消息
            } else if (e.state() == IdleState.WRITER_IDLE) {
                //log.debug("写消息闲置,ctx={}" + ctx);
                write(clusterPingMessage);
                // ctx.close();
            } else {
                log.debug("连接空闲时间到,ctx={}", ctx);
                ctx.close();
            }
        }
    }

    @Override
    public void messageReceived(Object obj) {
        ClusterMessage msg = (ClusterMessage)obj;
        if (msg.msg.messageType == 1 && msg.msg.cmd == 2) {
            //log.debug("收到心跳回包消息,ctx={}" + ctx);
        } else {
            clusterMessageDispatcher.onClusterReceive(this, msg);
        }
    }

    @Override
    public void onClose() {
        //TODO
    }

    @Override
    public void onCreate() {
        if (nodePath != null) {
            ClusterRegisterMsg clusterRegisterMsg = new ClusterRegisterMsg();
            clusterRegisterMsg.nodePath = nodePath;
            PFMessage pfMessage = MessageUtil.getPFMessage(clusterRegisterMsg);
            ClusterMessage clusterMessage = new ClusterMessage(pfMessage);
            write(clusterMessage);
        }
    }
}
