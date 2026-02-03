package de.kseek.core.protostuff;

import com.esotericsoftware.reflectasm.MethodAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import de.kseek.core.util.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Slf4j
public class MessageUtil {

    public static Map<Class<?>, ProtobufMessage> responseMap;

    public static PFMessage getPFMessage(PFResult pfResult) {
        PFMessage pfMessage = null;
        if (pfResult.getMsg() != null) {
            pfMessage = getPFMessage(pfResult.getMsg());
        } else {
            pfMessage = getPFMessage(pfResult.getClazz());
        }
        if (pfMessage != null) {
            pfMessage.setResultCode(pfResult.getCode());
        }
        return pfMessage;
    }

    public static PFMessage getPFMessage(Class<?> msgClass) {
        ProtobufMessage responseMessage = responseMap.get(msgClass);
        if (responseMessage == null) {
            log.warn("消息发送失败，该消息结构没有被ResponseMessage注解，msg-class={}", msgClass);
            return null;
        }
        return new PFMessage(responseMessage.messageType(), responseMessage.cmd(), null);
    }

    public static PFMessage getPFMessage(Object msg) {
        ProtobufMessage responseMessage = responseMap.get(msg.getClass());
        if (responseMessage == null) {
            log.warn("消息发送失败，该消息结构没有被ResponseMessage注解，msg-class={}", msg.getClass());
            return null;
        }
        byte[] data = ProtostuffUtil.serialize(msg);
        return new PFMessage(responseMessage.messageType(), responseMessage.cmd(), data);
    }

    public static Map<Integer, MessageController> load(ApplicationContext context) {
        Map<Integer, MessageController> messageControllers = new HashMap<>();
        Class<MessageType> clazz = MessageType.class;
        log.debug("开始初始化 {} 消息分发器", clazz);
        Map<String, Object> beans = context.getBeansWithAnnotation(clazz);
        beans.values().forEach(o -> {

            MessageType messageType = null;
            if (AopUtils.isAopProxy(o)) {
                Class<?> targetclazz = AopUtils.getTargetClass(o);
                messageType = targetclazz.getAnnotation(MessageType.class);
                MessageController messageController = new MessageController(o, targetclazz);
                messageControllers.put(messageType.value(), messageController);
            } else {
                messageType = o.getClass().getAnnotation(MessageType.class);
                MessageController messageController = new MessageController(o);
                messageControllers.put(messageType.value(), messageController);
            }

        });
        return messageControllers;
    }

    public static Map<Integer, MethodInfo> load(MethodAccess methodAccess, Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        Map<Integer, MethodInfo> methodInfos = new HashMap<>();
        for (Method method : methods) {
            Class<Command> clz = Command.class;
            Command command = method.getAnnotation(clz);
            if (command != null) {
                String name = method.getName();
                Class[] types = method.getParameterTypes();
                Type returnType = method.getReturnType();
                int index = methodAccess.getIndex(name, types);
                MethodInfo methodInfo = new MethodInfo(index, name, types, returnType);
                methodInfos.put(command.value(), methodInfo);
            }

        }
        return methodInfos;
    }

    public static Map<Class<?>, ProtobufMessage> loadResponseMessage(String... pkgs) {
        responseMap = new HashMap<>();
        Set<Class<?>> clazzSet = new HashSet<>();
        for (String pkg : pkgs) {
            clazzSet.addAll(ClassUtils.getAllClassByAnnotation(pkg, ProtobufMessage.class));
        }
        if (!clazzSet.isEmpty()) {
            clazzSet.forEach(clazz -> {
                ProtobufMessage responseMessage = clazz.getAnnotation(ProtobufMessage.class);
                if (responseMessage.resp()) {
                    responseMap.put(clazz, responseMessage);
                }
            });
        }
        return responseMap;
    }
}
