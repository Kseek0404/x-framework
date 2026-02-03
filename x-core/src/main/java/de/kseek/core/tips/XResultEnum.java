package de.kseek.core.tips;

import de.kseek.core.protostuff.ProtobufMessage;

/**
 * @author kseek
 * @date 2024/3/22
 */
@ProtobufMessage
public enum XResultEnum {
    NETWORK_CANT_USE(0, "网络不可用"),
    NETWORK_SUCCESS(1, "连接成功"),
    SERVICE_CANT_USE(2, "服务不可用"),
    PLAYER_KICK_OUT(3, "踢出下线");

    public final int code;
    public final String message;

    XResultEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
