package de.kseek.game.common.message;

import de.kseek.core.protostuff.ProtobufMessage;
import de.kseek.game.common.GameMessageConst;
import lombok.Data;

/**
 * 进入大厅响应
 */
@Data
@ProtobufMessage(resp = true, messageType = GameMessageConst.HALL_TYPE, cmd = GameMessageConst.HALL_ENTER_RESP)
public class RespEnterHall {
    private boolean success;
    private String welcome;
}
