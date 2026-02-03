package de.kseek.core.protostuff;

import lombok.Data;

/**
 * @author kseek
 * @date 2024/3/22
 */
@ProtobufMessage
@Data
public class PFMessage {
    @ProtobufComment("消息类型")
    public int messageType;
    /* 子命令字*/
    @ProtobufComment("命令号")
    public int cmd;
    /* 数据*/
    @ProtobufComment("数据")
    public byte[] data;
    @ProtobufComment("请求消息ID")
    public long msgId;
    @ProtobufComment("返回码")
    public long resultCode;

    public PFMessage() {
    }

    public PFMessage(int messageType, int cmd, byte[] data) {
        this.messageType = messageType;
        this.cmd = cmd;
        this.data = data;
    }

    @Override
    public String toString() {
        return "PFMessage{" +
                "messageType=" + messageType +
                ", cmd=" + cmd +
                '}';
    }
}
