package de.kseek.game.common.message;

import de.kseek.core.protostuff.ProtobufMessage;
import de.kseek.game.common.GameMessageConst;
import lombok.Data;

/**
 * 登录响应
 */
@Data
@ProtobufMessage(resp = true, messageType = GameMessageConst.LOGIN_TYPE, cmd = GameMessageConst.LOGIN_RESP)
public class RespLogin {
    /** 是否成功 */
    private boolean success;
    /** 用户ID（成功时） */
    private long userId;
    /** 提示信息（失败时） */
    private String message;
}
