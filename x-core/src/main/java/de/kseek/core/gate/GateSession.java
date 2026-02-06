package de.kseek.core.gate;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import de.kseek.core.cluster.ClusterClient;
import de.kseek.core.cluster.ClusterMessage;
import de.kseek.core.cluster.ClusterSystem;
import de.kseek.core.constant.MessageConst;
import de.kseek.core.curator.NodeType;
import de.kseek.core.message.SessionCreate;
import de.kseek.core.message.SessionLogout;
import de.kseek.core.message.SessionQuit;
import de.kseek.core.net.Connect;
import de.kseek.core.net.ConnectListener;
import de.kseek.core.net.Inbox;
import de.kseek.core.net.NetAddress;
import de.kseek.core.netty.NettyConnect;
import de.kseek.core.protostuff.MessageUtil;
import de.kseek.core.protostuff.PFMessage;
import de.kseek.core.protostuff.PingMessageHandler;
import de.kseek.core.protostuff.ProtostuffUtil;
import de.kseek.core.tips.XResultEnum;
import de.kseek.core.tips.RespServerStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Slf4j
public class GateSession extends NettyConnect implements ConnectListener, Inbox<PFMessage> {
    public static Map<String, GateSession> gateSessionMap = new HashMap<>();
    /**
     * 大厅通道
     */
    private ClusterClient currentClient;
    /**
     * 会话ID
     */
    public String sessionId;
    /**
     * 用户ID
     */
    public long userId;
    /**
     * 是否已认证
     */
    public boolean certify;

    public long activeTime;

    public long createTime;

    public Connect connect;

    // 最后一条响应消息
    private PFMessage cacheMessage;

    public GateSession() {
    }

    /**
     * 收到集群中其他节点发送过来的消息
     *
     * @param msg
     */
    @Override
    public void onClusterReceive(Connect connect, PFMessage msg) {
        // 根据消息ID进行缓存
        if (msg.msgId > 0) {
            cacheMessage = msg;
        }
        log.debug("收到转发消息，sessionId={}，messageType={},cmd={}", sessionId, msg.messageType, msg.cmd);
        write(msg);
    }

    /**
     * 接收客户端发送过来的消息
     *
     * @param obj
     */
    @Override
    public void messageReceived(Object obj) {
        PFMessage msg = (PFMessage) obj;
        if (msg.messageType == MessageConst.ToClientConst.TYPE && msg.cmd == MessageConst.ToClientConst.REQ_SERVER_STATUS) {
            checkServer();
            return;
        }
        // 如果当前没有认证并且当前消息不是认证消息，断开连接
        if (!certify && msg.messageType != 1000) {
            log.warn("未认证状态，消息错误，message={}", msg);
            close();
            return;
        }
        // 拦截心跳 Ping，直接回 Pong
        if (msg.messageType == MessageConst.HeartbeatConst.TYPE && msg.cmd == MessageConst.HeartbeatConst.CMD_PING) {
            activeTime = System.currentTimeMillis();
            PFMessage pfMessage = new PFMessage(MessageConst.HeartbeatConst.TYPE, MessageConst.HeartbeatConst.CMD_PONG, ProtostuffUtil.serialize(new PingMessageHandler.Pong(activeTime)));
            pfMessage.setMsgId(msg.msgId);
            write(pfMessage);
            return;
        }

        // 获取缓存消息
        if (msg.msgId > 0 && cacheMessage != null && cacheMessage.msgId == msg.msgId
                && cacheMessage.messageType == msg.messageType && cacheMessage.cmd == msg.cmd + 1) {
            write(cacheMessage);
            return;
        }
        log.debug("收到客户端消息，sessionId={}，messageType={},cmd={}", sessionId, msg.messageType, msg.cmd);
        ClusterMessage clusterMessage = new ClusterMessage(sessionId, msg, userId);
        // 查询微服务消息
        try {
            Connect micConnect = ClusterSystem.getInstance().micServiceAllot(currentClient, msg.messageType);
            if (micConnect != null) {
                micConnect.write(clusterMessage);
                return;
            }
        } catch (Exception e) {
            log.debug("微服务消息发送失败,messageType={}", msg.messageType, e);
        }
        if (connect == null || !connect.isActive()) {
            if (connect != null) {
                currentClient.close(connect);
            }
            connect = getConnect();
        }
        connect.write(clusterMessage);
    }

    /**
     * 用户连接关闭
     */
    @Override
    public void onClose() {
        log.debug("连接断开,sessionId={}", sessionId);
        gateSessionMap.remove(sessionId);
        sendClose();
        if (userId > 0) {
            // 移除session
            //向登录服务器发送用户下线
            sendLogout();
        }
        if (connect != null) {
            connect.removeConnectListener(this);
        }
        this.currentClient = null;
        this.connect = null;
    }

    public void sendLogout() {
        try {
            SessionLogout sessionLogout = new SessionLogout();
            sessionLogout.sessionId = sessionId;
            sessionLogout.playerId = userId;
            PFMessage pfMessage = MessageUtil.getPFMessage(sessionLogout);
            ClusterMessage clusterMessage = new ClusterMessage(sessionId, pfMessage, userId);
            ClusterClient clusterClient = ClusterSystem.getInstance().getByNodeType(NodeType.ACCOUNT, remoteAddress.getHost(), userId);
            if (currentClient != null) {
                clusterClient.getConnect().write(clusterMessage);
            }
        } catch (Exception e) {
            log.warn("用户下线消息发送异常", e);
        }
    }

    /**
     * 向当前连接节点发送关闭消息
     */
    private void sendClose() {
        if (connect == null || currentClient == null) {
            if (certify) {
                log.warn("向集群节点发送退出消息失败，连接为空");
            }
            return;
        }
        SessionQuit sessionQuit = new SessionQuit();
        sessionQuit.sessionId = sessionId;
        PFMessage pfMessage = MessageUtil.getPFMessage(sessionQuit);
        ClusterMessage clusterMessage = new ClusterMessage(sessionId, pfMessage, userId);
        connect.write(clusterMessage);
    }

    /**
     * 向当前连接节点发送进入消息
     */
    private void sendEnter(String srcNodeTypeStr) {
        if (connect == null || currentClient == null) {
            log.warn("向集群节点发送进入消息失败，连接为空");
            return;
        }
        log.debug("向集群节点发送进入消息,nodeName={},nodeAddress={}", currentClient.getRemoteNodeConfig().getName(), currentClient.getRemoteNodeConfig().getTcpAddress());
        SessionCreate sessionCreate = new SessionCreate();
        sessionCreate.sessionId = sessionId;
        sessionCreate.netAddress = remoteAddress;
        sessionCreate.userId = userId;
        sessionCreate.nodePath = ClusterSystem.getInstance().getNodePath();
        sessionCreate.srcNodeTypeStr = srcNodeTypeStr;
        PFMessage pfMessage = MessageUtil.getPFMessage(sessionCreate);
        ClusterMessage clusterMessage = new ClusterMessage(sessionId, pfMessage, userId);
        connect.write(clusterMessage);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                String sessionId = ctx.channel().id().asShortText();
                GateSession gateSession = GateSession.gateSessionMap.get(sessionId);
                if (gateSession != null) {
                    log.warn("连接读闲置时间到，即将被关闭,activeTime={},ctx={}", gateSession.activeTime, ctx);
                } else {
                    log.warn("连接读闲置时间到，即将被关闭,ctx={}", ctx);
                }
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 用户连接建立
     */
    @Override
    public void onCreate() {
        sessionId = channel.id().asShortText();
        log.debug("连接成功，检查服务器状态，sessionId={},net={}", sessionId, remoteAddress);
        String ip = remoteAddress.getHost();
        remoteAddress.setHost(ip);
    }

    protected void checkServer() {
        log.debug("收到服务器检查消息，sessionId={},netAddress={}", sessionId, remoteAddress);
        activeTime = createTime = System.currentTimeMillis();
        // 将session添加到集群节点中
        gateSessionMap.put(sessionId, this);
        //连接建立成功以后，分配一个账号服务器进行账号认证
        currentClient = ClusterSystem.getInstance().getByNodeType(NodeType.ACCOUNT, remoteAddress.getHost(), userId);
        sendServerStatus();
    }


    public void sendServerStatus() {
        if (currentClient == null) {
            log.debug("找不到可用的登录服务器");
            write(MessageUtil.getPFMessage(new RespServerStatus(XResultEnum.NETWORK_CANT_USE)));
            return;
        }
        connect = getConnect();
        if (connect == null) {
            log.debug("登录服务器连接不可用");
            write(MessageUtil.getPFMessage(new RespServerStatus(XResultEnum.NETWORK_CANT_USE)));
            return;
        }
        sendEnter(null);
        write(MessageUtil.getPFMessage(new RespServerStatus(XResultEnum.NETWORK_SUCCESS)));
    }

    public void switchNode(ClusterClient clusterClient) {
        log.debug("切换节点,sessionId={},userId={},srcPath={},targetPath={},certify={}", sessionId, userId,
                this.currentClient.getRemoteNodeConfig().getName(), clusterClient.getRemoteNodeConfig().getName(), certify);
        if (certify) {
            String srcNodeTypeStr = this.currentClient.getRemoteNodeConfig().getType();
            //向源节点发送退出
            sendClose();
            this.currentClient = clusterClient;
            this.connect = getConnect();
            sendEnter(srcNodeTypeStr);
        }
    }

    public void onKickOut() {
        log.info("用户被顶号下线，sessionId={}，playerId={}", sessionId, userId);
        userId = 0;
        try {
            writeAndClose(MessageUtil.getPFMessage(new RespServerStatus(XResultEnum.PLAYER_KICK_OUT)));
        } catch (Exception e) {
            log.warn("用户被顶号下线,消息发送异常", e);
        }
    }

    @Override
    public void onConnectClose(Connect connect) {
        log.warn("服务节点连接断开,userId={},sessionId={},nodeAddress={}", userId, sessionId, connect.address());
        try {
            //ctx.close();
            writeAndClose(MessageUtil.getPFMessage(new RespServerStatus(XResultEnum.NETWORK_CANT_USE)));
        } catch (Exception e) {
            log.warn("服务节点连接断开,消息发送异常，", e);
        }
    }

    public Connect getConnect() {
        if (connect != null) {
            connect.removeConnectListener(this);
        }
        try {
            connect = currentClient.getConnectSync();
            connect.addConnectListener(this);
        } catch (Exception e) {
            log.warn("集群客户端获取连接异常,nodePath=" + currentClient.xNode.getNodePath(), e);
            write(MessageUtil.getPFMessage(new RespServerStatus(XResultEnum.NETWORK_CANT_USE)));
            gateSessionMap.remove(sessionId);
            close();
        }
        return connect;
    }

    public boolean isActive() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - createTime > 90000 && !certify) {
            return false;
        }
        if (currentTime - activeTime > 150000) {
            return false;
        }
        return true;
    }

    public void setHost(String hostIp) {
        if (hostIp != null && !hostIp.isEmpty()) {
            remoteAddress = new NetAddress(hostIp, remoteAddress != null ? remoteAddress.getPort() : 0);
        }
    }

    @Override
    public String toString() {
        return "GateSession{" +
                "sessionId='" + sessionId + '\'' +
                ", userId=" + userId +
                ", certify=" + certify +
                ", connect=" + connect +
                '}';
    }
}
