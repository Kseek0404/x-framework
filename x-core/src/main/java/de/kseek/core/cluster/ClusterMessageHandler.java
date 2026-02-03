package de.kseek.core.cluster;

import de.kseek.core.message.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import de.kseek.core.constant.MessageConst;
import de.kseek.core.executor.XWorkExecutor;
import de.kseek.core.gate.GateSession;
import de.kseek.core.listener.SessionCloseListener;
import de.kseek.core.listener.SessionEnterListener;
import de.kseek.core.listener.SessionLogoutListener;
import de.kseek.core.listener.SessionVerifyListener;
import de.kseek.core.net.Connect;
import de.kseek.core.net.NetAddress;
import de.kseek.core.netty.NettyConnect;
import de.kseek.core.protostuff.Command;
import de.kseek.core.protostuff.MessageType;
import de.kseek.core.protostuff.PFSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Slf4j
@Component
@MessageType(MessageConst.SessionConst.TYPE)
@Data
@RequiredArgsConstructor
public class ClusterMessageHandler {

    /**
     * 集群总线
     */
    private final ClusterSystem clusterSystem;
    /**
     * session 验证监听器
     */
    private final Optional<SessionVerifyListener> sessionVerifyListener;
    /**
     * session 进入监听器
     */
    private final Optional<SessionEnterListener> sessionEnterListener;
    /**
     * session 退出监听器
     */
    private final Optional<SessionCloseListener> sessionCloseListener;
    /**
     * session 登出监听器
     */
    private final Optional<SessionLogoutListener> sessionLogoutListener;

    private final XWorkExecutor xWorkExecutor;

    /**
     * 连接断开退出
     */
    @Command(MessageConst.SessionConst.NOTIFY_SESSION_QUIT)
    public void sessionClose(SessionQuit sessionQuit) {
        String sessionId = sessionQuit.sessionId;
        log.debug("用户连接退出，sessionId={}", sessionId);
        PFSession pfSession = clusterSystem.sessionMap().remove(sessionId);
//        clusterSystem.sessionMap().keySet().forEach(log::info);
        if (pfSession == null) {
            log.warn("用户连接退出异常，找不到session,sessionId={}", sessionId);
        }
        if (pfSession != null) {
            sessionCloseListener.ifPresent(listener -> listener.sessionClose(pfSession));
        }
    }

    /**
     * 认证成功后通知给网关服务器
     *
     * @param sessionVerifyPass
     */
    @Command(MessageConst.SessionConst.NOTIFY_SESSION_VERIFY_PASS)
    public void sessionVerifyPass(SessionVerifyPass sessionVerifyPass) {
        sessionVerifyListener.ifPresent(listener -> listener.userVerifyPass(sessionVerifyPass.sessionId, sessionVerifyPass.userId, sessionVerifyPass.ip));
    }

    /**
     * 收到session进入消息
     *
     * @param sessionCreate
     */
    @Command(MessageConst.SessionConst.NOTIFY_SESSION_ENTER)
    public void sessionEnter(PFSession pfSession, Connect connect, SessionCreate sessionCreate) {
        String sessionId = sessionCreate.sessionId;
        long userId = sessionCreate.userId;
        String gatePath = sessionCreate.nodePath;
        NetAddress netAddress = sessionCreate.netAddress;
        log.info("用户连接进入，sessionId={},netAddress={},gatePath={}", sessionId, netAddress, gatePath);
        if (pfSession == null) {
            pfSession = new PFSession(sessionId, connect, sessionCreate.netAddress);
        }
        pfSession.setAddress(sessionCreate.netAddress);
        clusterSystem.sessionMap().put(sessionId, pfSession);
        pfSession.gatePath = gatePath;
        PFSession finalPfSession = pfSession;
        sessionEnterListener.ifPresent(listener -> listener.sessionEnter(finalPfSession, userId, sessionCreate.srcNodeTypeStr));
    }

    /**
     * session下线
     *
     * @param sessionLogout
     */
    @Command(MessageConst.SessionConst.NOTIFY_SESSION_LOGOUT)
    public void sessionLogout(Connect connect, SessionLogout sessionLogout) {
        String sessionId = sessionLogout.sessionId;
        long playerId = sessionLogout.playerId;
        log.info("用户下线，sessionId={}，playerId={}", sessionId, playerId);
        sessionLogoutListener.ifPresent(listener -> listener.logout(playerId, sessionId));
    }

    /**
     * 踢出用户下线
     *
     * @param sessionKickOut
     */
    @Command(MessageConst.SessionConst.NOTIFY_SESSION_KICK_OUT)
    public void sessionKickOut(Connect connect, SessionKickOut sessionKickOut) {
        String sessionId = sessionKickOut.sessionId;
        long playerId = sessionKickOut.playerId;
        log.info("用户被顶号下线，sessionId={}，playerId={}", sessionId, playerId);
        GateSession gateSession = GateSession.gateSessionMap.get(sessionId);
        if (gateSession != null) {
            gateSession.onKickOut();
        }
    }

    @Command(MessageConst.SessionConst.CLUSTER_CONNECT_REGISTER)
    public void clusterRegister(NettyConnect connect, ClusterRegisterMsg clusterRegisterMsg) {
        if (clusterRegisterMsg == null) {
            log.debug("节点注册异常,connect={}", connect);
            return;
        }
        String nodePath = clusterRegisterMsg.nodePath;
        ClusterClient clusterClient = clusterSystem.getClusterByPath(nodePath);
        if (clusterClient != null) {
            clusterClient.connectPool.addConnect(connect);
            log.debug("节点注册成功,nodePath={},connect={}", nodePath, connect);
        }
    }

    @Command(MessageConst.SessionConst.NOTIFY_SWITCH_NODE)
    public void switchNode(SwitchNodeMessage switchNodeMessage) {
        String targetNodePath = switchNodeMessage.targetNodePath;
        String sessionId = switchNodeMessage.sessionId;
        long userId = switchNodeMessage.userId;
        ClusterClient clusterClient = clusterSystem.getClusterByPath(targetNodePath);
        GateSession gateSession = GateSession.gateSessionMap.get(sessionId);
        if (gateSession != null && clusterClient != null) {
            gateSession.switchNode(clusterClient);
        } else {
            log.warn("找不到gate session，sessionId={},gateSession={}", sessionId, gateSession);
            //GateSession.gateSessionMap.forEach((k,v)-> log.debug(k + "->" + v));
        }
    }

    /**
     * 该消息需要广播给所有用户
     *
     * @param broadCastMessage
     */
    @Command(MessageConst.SessionConst.BROADCAST_MSG)
    public void broadcast(BroadCastMessage broadCastMessage) {
        List<GateSession> gateSessionList = new ArrayList<>(GateSession.gateSessionMap.values());
        for (GateSession session : gateSessionList) {
            //广播给已经认证的用户
            if (session != null && session.isActive() && session.certify) {
                session.write(broadCastMessage.msg);
            }
        }
    }
}
