package de.kseek.core.protostuff;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import de.kseek.core.cluster.ClusterMessage;
import de.kseek.core.executor.TaskGroup;
import de.kseek.core.message.SessionVerifyPass;
import de.kseek.core.net.Connect;
import de.kseek.core.net.NetAddress;
import de.kseek.core.net.Session;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Slf4j
@Setter
@Getter
public class PFSession extends Session<Object, ClusterMessage> {

    public final static AtomicLong WORK_ID = new AtomicLong();

    public long userId;
    /* 网关节点PATH*/
    public String gatePath;
    /* 业务ID，用于根据该ID分配业务线程*/
    public long workId;
    /* 活动时间*/
    public long activeTime;
    /* 任务组*/
    private TaskGroup taskGroup;

    public PFSession() {
    }

    public PFSession(String sessionId, Connect connect, NetAddress address) {
        super(sessionId, connect, address);
        activeTime = System.currentTimeMillis();
        this.workId = WORK_ID.incrementAndGet();
        if (workId > 1000) {
            WORK_ID.set(1);
        }
    }

    /**
     * 发送消息
     *
     * @param msg
     */
    @Override
    public void send(Object msg) {
        writeMsg(0, msg);
    }

    /**
     * 发送消息到网关
     *
     * @param msg
     */
    public void send2Gate(Object msg) {
        PFMessage pfMessage = MessageUtil.getPFMessage(msg);
        ClusterMessage clusterMessage = new ClusterMessage(pfMessage);
        connect.write(clusterMessage);
    }

    /**
     * 响应指定id 的消息
     *
     * @param id
     * @param msg
     */
    public void response(long id, Object msg) {
        writeMsg(id, msg);
    }

    private void writeMsg(long id, Object msg) {
        PFMessage pfMessage;
        if (msg instanceof PFMessage) {
            pfMessage = (PFMessage) msg;
        } else if (msg instanceof PFResult) {
            pfMessage = MessageUtil.getPFMessage((PFResult) msg);
        } else {
            pfMessage = MessageUtil.getPFMessage(msg);
        }
        if (pfMessage != null) {
            pfMessage.setMsgId(id);
            ClusterMessage clusterMessage = new ClusterMessage(sessionId, pfMessage);
            connect.write(clusterMessage);
        }
    }

    /**
     * 当用户验证通过后调用
     *
     * @param userId
     * @param reference
     */
    public void verifyPass(long userId, String ip, Object reference) {
        this.reference = reference;
        this.userId = userId;
        SessionVerifyPass sessionVerifyPass = new SessionVerifyPass();
        sessionVerifyPass.userId = userId;
        sessionVerifyPass.sessionId = sessionId;
        sessionVerifyPass.ip = ip;
        sessionVerifyPass.create = System.currentTimeMillis();
        PFMessage pfMessage = MessageUtil.getPFMessage(sessionVerifyPass);
        ClusterMessage clusterMessage = new ClusterMessage(pfMessage);
        connect.write(clusterMessage);
    }

    public void onClose() {
        sessionListener.onSessionClose(this);
    }
}
