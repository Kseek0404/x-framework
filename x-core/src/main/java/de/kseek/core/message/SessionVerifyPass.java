package de.kseek.core.message;

import de.kseek.core.constant.MessageConst;
import de.kseek.core.protostuff.ProtobufMessage;

/**
 * @author kseek
 * @date 2024/3/22
 */
@ProtobufMessage(resp = true,messageType = MessageConst.SessionConst.TYPE, cmd = MessageConst.SessionConst.NOTIFY_SESSION_VERIFY_PASS)
public class SessionVerifyPass {
    public String sessionId;
    public long userId;
    public long create;
    public String ip;
}
