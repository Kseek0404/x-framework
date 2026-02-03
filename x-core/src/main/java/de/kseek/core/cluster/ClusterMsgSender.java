package de.kseek.core.cluster;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import de.kseek.core.message.BroadCastMessage;
import de.kseek.core.protostuff.MessageUtil;
import de.kseek.core.protostuff.PFMessage;

import java.util.List;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ClusterMsgSender {

    private final ClusterSystem clusterSystem;

    /**
     * 向所有网关广播消息
     *
     * @param msg
     */
    public void broadcast2Gates(Object msg) {
        List<ClusterClient> clusterClients = clusterSystem.getAllGate();
        if (clusterClients != null && !clusterClients.isEmpty()) {
            clusterClients.forEach(clusterClient -> {
                try {
                    PFMessage pfmsg = MessageUtil.getPFMessage(msg);
                    PFMessage pfMessage = MessageUtil.getPFMessage(new BroadCastMessage(pfmsg));
                    ClusterMessage clusterMessage = new ClusterMessage(pfMessage);
                    clusterClient.write(clusterMessage);
                } catch (Exception e) {
                    log.warn("广播消息到网关失败,gateName={}", clusterClient.getRemoteNodeConfig().getName(), e);
                }
            });
        }
    }
}
