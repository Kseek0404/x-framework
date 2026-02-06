package de.kseek.core.protostuff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class PFMessageManager {
    public static Map<Class<?>, ProtobufMessage> responseMap;

    public static Logger log = LoggerFactory.getLogger(PFMessageManager.class);

    public static PFMessage getPFMessage(Object msg) {
        ProtobufMessage responseMessage = responseMap.get(msg.getClass());
        if (responseMessage == null) {
            log.warn("消息发送失败，该消息结构没有被ResponseMessage注解，msg-class={}", msg.getClass());
            return null;
        }
        byte[] data = ProtostuffUtil.serialize(msg);
        return new PFMessage(responseMessage.messageType(), responseMessage.cmd(), data);
    }

}
