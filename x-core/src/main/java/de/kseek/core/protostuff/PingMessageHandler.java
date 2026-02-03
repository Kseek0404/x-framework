package de.kseek.core.protostuff;

import org.springframework.stereotype.Component;
import de.kseek.core.cluster.ClusterMessage;
import de.kseek.core.net.Connect;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Component
@MessageType(1)
public class PingMessageHandler {

    @ProtobufMessage(resp = false, messageType = 1, cmd = 1)
    public static class Ping {
        byte non;
    }

    @ProtobufMessage(resp = true, messageType = 1, cmd = 2)
    public static class Pong {
        long time;

        public Pong(long time) {
            this.time = time;
        }
    }

    @Command(1)
    public void ping(PFSession session, Connect connect) {
        if (session != null) {
            session.send(new Pong(System.currentTimeMillis()));
        } else if (connect != null) {
            Pong pong = new Pong(0);
            PFMessage pfMessage = new PFMessage(1, 2, ProtostuffUtil.serialize(pong));
            ClusterMessage clusterMessage = new ClusterMessage(pfMessage);
            connect.write(clusterMessage);
        }
    }
}
