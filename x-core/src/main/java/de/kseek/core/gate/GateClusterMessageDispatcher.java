package de.kseek.core.gate;

import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;
import de.kseek.core.cluster.ClusterMessage;
import de.kseek.core.cluster.ClusterMessageDispatcher;
import de.kseek.core.constant.MessageConst;
import de.kseek.core.net.Connect;
import de.kseek.core.protostuff.PFMessage;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Slf4j
@ChannelHandler.Sharable
public class GateClusterMessageDispatcher extends ClusterMessageDispatcher {

    @Override
    public void onClusterReceive(Connect connect, ClusterMessage clusterMessage) {
        String sessionId = clusterMessage.sessionId;
        PFMessage pfMessage = clusterMessage.msg;
        if (sessionId != null && !sessionId.isEmpty()) {
            GateSession gateSession = GateSession.gateSessionMap.get(sessionId);
            if (gateSession != null) {
                gateSession.onClusterReceive(connect, pfMessage);
            } else {
                log.warn("找不到sessionId={}的session，无法转发消息", sessionId);
            }
        } else {
            if (pfMessage != null && pfMessage.messageType == MessageConst.HeartbeatConst.TYPE && pfMessage.cmd == MessageConst.HeartbeatConst.CMD_PONG) {
                return;
            }
            super.handle(connect, null, pfMessage);
        }
    }
}
