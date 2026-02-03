package de.kseek.core.message;

import de.kseek.core.constant.MessageConst;
import de.kseek.core.protostuff.ProtobufMessage;

/**
 * @author kseek
 * @date 2024/3/22
 */
@ProtobufMessage(resp = true, messageType = MessageConst.SessionConst.TYPE, cmd = MessageConst.SessionConst.CLUSTER_CONNECT_REGISTER)
public class ClusterRegisterMsg {
    public String nodePath;
}
