package de.kseek.core.tips;

import de.kseek.core.constant.MessageConst;
import de.kseek.core.protostuff.ProtobufComment;
import de.kseek.core.protostuff.ProtobufMessage;

/**
 * @author kseek
 * @date 2024/3/22
 */
@ProtobufComment("请求服务器状态")
@ProtobufMessage(messageType = MessageConst.ToClientConst.TYPE, cmd = MessageConst.ToClientConst.REQ_SERVER_STATUS)
public class ReqServerStatus {
    public byte non;
}
