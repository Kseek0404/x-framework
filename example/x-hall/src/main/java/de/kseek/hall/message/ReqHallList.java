package de.kseek.hall.message;

import de.kseek.core.protostuff.ProtobufMessage;
import de.kseek.game.common.GameMessageConst;
import lombok.Data;

/**
 * 大厅列表请求
 */
@Data
@ProtobufMessage(messageType = GameMessageConst.HALL_TYPE, cmd = GameMessageConst.HALL_LIST_REQ)
public class ReqHallList {
    private int page;
    private int pageSize;
}
