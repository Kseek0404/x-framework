package de.kseek.core.net;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import de.kseek.core.protostuff.ProtobufMessage;

/**
 * @author kseek
 * @date 2024/3/22
 */
@ProtobufMessage
@Data
public class NetAddress {

    private String host;

    private int port;

    public NetAddress() {
    }

    public NetAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 获取地址，ip:端口
     *
     * @return
     */
    @JSONField(serialize = false)
    public String getStrAddress() {
        return host + ":" + port;
    }
}
