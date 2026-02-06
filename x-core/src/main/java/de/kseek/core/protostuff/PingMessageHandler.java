package de.kseek.core.protostuff;

import org.springframework.stereotype.Component;
import de.kseek.core.cluster.ClusterMessage;
import de.kseek.core.constant.MessageConst;
import de.kseek.core.net.Connect;

/**
 * 框架心跳：Ping(请求) / Pong(响应)，messageType={@link MessageConst.HeartbeatConst#TYPE}，cmd 1/2。
 *
 * @author kseek
 * @date 2024/3/22
 */
@Component
@MessageType(MessageConst.HeartbeatConst.TYPE)
public class PingMessageHandler {

    @ProtobufMessage(resp = false, messageType = MessageConst.HeartbeatConst.TYPE, cmd = MessageConst.HeartbeatConst.CMD_PING)
    public static class Ping {
        byte non;
    }

    @ProtobufMessage(resp = true, messageType = MessageConst.HeartbeatConst.TYPE, cmd = MessageConst.HeartbeatConst.CMD_PONG)
    public static class Pong {
        long time;

        public Pong(long time) {
            this.time = time;
        }
    }

    @Command(MessageConst.HeartbeatConst.CMD_PING)
    public void ping(PFSession session, Connect connect) {
        if (session != null) {
            session.send(new Pong(System.currentTimeMillis()));
        } else if (connect != null) {
            Pong pong = new Pong(0);
            PFMessage pfMessage = new PFMessage(MessageConst.HeartbeatConst.TYPE, MessageConst.HeartbeatConst.CMD_PONG, ProtostuffUtil.serialize(pong));
            ClusterMessage clusterMessage = new ClusterMessage(pfMessage);
            connect.write(clusterMessage);
        }
    }
}
