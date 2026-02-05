package de.kseek.hall.message;

import de.kseek.core.protostuff.ProtobufMessage;
import de.kseek.game.common.GameMessageConst;
import lombok.Data;

/**
 * 进入大厅请求
 */
@Data
@ProtobufMessage(messageType = GameMessageConst.HALL_TYPE, cmd = GameMessageConst.HALL_ENTER_REQ)
public class ReqEnterHall {
    private long userId;
}
