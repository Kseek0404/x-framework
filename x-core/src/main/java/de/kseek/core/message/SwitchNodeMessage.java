package de.kseek.core.message;

import de.kseek.core.constant.MessageConst;
import de.kseek.core.protostuff.ProtobufMessage;

/**
 * @author kseek
 * @date 2024/3/22
 */
@ProtobufMessage(resp = true, messageType = MessageConst.SessionConst.TYPE, cmd = MessageConst.SessionConst.NOTIFY_SWITCH_NODE)
public class SwitchNodeMessage {
    public String sessionId;
    //目标节点路径
    public String targetNodePath;

    public long userId;

    public SwitchNodeMessage(String sessionId, String targetNodePath, long userId) {
        this.sessionId = sessionId;
        this.targetNodePath = targetNodePath;
        this.userId = userId;
    }
}
