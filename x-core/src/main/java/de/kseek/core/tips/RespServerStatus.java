package de.kseek.core.tips;

import lombok.Data;
import de.kseek.core.constant.MessageConst;
import de.kseek.core.protostuff.ProtobufComment;
import de.kseek.core.protostuff.ProtobufMessage;

/**
 * @author kseek
 * @date 2024/3/22
 */
@ProtobufMessage(resp = true, messageType = MessageConst.ToClientConst.TYPE, cmd = MessageConst.ToClientConst.RESP_SERVER_STATUS)
@Data
@ProtobufComment("返回服务器状态")
public class RespServerStatus {

    @ProtobufComment("服务器状态")
    private XResultEnum result;
    @ProtobufComment("服务器秒级时间")
    private int serverSecTime;

    public RespServerStatus(XResultEnum result) {
        this.result = result;
        this.serverSecTime = (int) (System.currentTimeMillis() / 1000L);
    }
}
