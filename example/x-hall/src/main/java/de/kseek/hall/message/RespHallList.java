package de.kseek.hall.message;

import de.kseek.core.protostuff.ProtobufMessage;
import de.kseek.game.common.GameMessageConst;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 大厅列表响应
 */
@Data
@ProtobufMessage(resp = true, messageType = GameMessageConst.HALL_TYPE, cmd = GameMessageConst.HALL_LIST_RESP)
public class RespHallList {
    private List<HallRoomItem> list = new ArrayList<>();
    private int total;

    @Data
    public static class HallRoomItem {
        private int roomId;
        private String name;
        private int onlineCount;
    }
}
