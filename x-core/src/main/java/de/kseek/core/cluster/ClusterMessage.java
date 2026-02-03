package de.kseek.core.cluster;

import de.kseek.core.protostuff.PFMessage;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class ClusterMessage {
    public String sessionId;
    public PFMessage msg;
    public long userId;

    public ClusterMessage(PFMessage msg) {
        this.msg = msg;
    }

    public ClusterMessage(String sessionId, PFMessage msg) {
        this.sessionId = sessionId;
        this.msg = msg;
    }

    public ClusterMessage(String sessionId, PFMessage msg, long userId) {
        this.sessionId = sessionId;
        this.msg = msg;
        this.userId = userId;
    }
}
