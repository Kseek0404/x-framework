package de.kseek.login.message;

import de.kseek.core.protostuff.ProtobufMessage;
import de.kseek.game.common.GameMessageConst;
import lombok.Data;

/**
 * 登录请求
 */
@Data
@ProtobufMessage(messageType = GameMessageConst.LOGIN_TYPE, cmd = GameMessageConst.LOGIN_REQ)
public class ReqLogin {
    /** 账号 */
    private String account;
    /** 密码（示例中明文，实际应加密） */
    private String password;
}
