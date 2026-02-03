package de.kseek.hall;

import de.kseek.core.micservice.MicService;
import de.kseek.core.protostuff.Command;
import de.kseek.core.protostuff.MessageType;
import de.kseek.core.protostuff.PFSession;
import de.kseek.game.common.GameMessageConst;
import de.kseek.game.common.message.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 大厅微服务 - 处理 messageType=2000，供 Gate 转发
 */
@Slf4j
@Component
@MicService
@MessageType(GameMessageConst.HALL_TYPE)
public class HallService {

    @Command(GameMessageConst.HALL_ENTER_REQ)
    public RespEnterHall enterHall(PFSession session, ReqEnterHall req) {
        RespEnterHall resp = new RespEnterHall();
        resp.setSuccess(true);
        resp.setWelcome("欢迎进入大厅，userId=" + session.getUserId());
        log.info("用户进入大厅 userId={}", session.getUserId());
        return resp;
    }

    @Command(GameMessageConst.HALL_LIST_REQ)
    public RespHallList hallList(PFSession session, ReqHallList req) {
        RespHallList resp = new RespHallList();
        int page = req != null && req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req != null && req.getPageSize() > 0 ? req.getPageSize() : 10;
        List<RespHallList.HallRoomItem> list = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            RespHallList.HallRoomItem item = new RespHallList.HallRoomItem();
            item.setRoomId(1000 + i);
            item.setName("房间" + i);
            item.setOnlineCount(i * 2);
            list.add(item);
        }
        resp.setList(list);
        resp.setTotal(5);
        log.debug("大厅列表 userId={}, page={}", session.getUserId(), page);
        return resp;
    }
}
