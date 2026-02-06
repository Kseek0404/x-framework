package de.kseek.core.cluster;

import de.kseek.core.protostuff.*;
import io.netty.channel.ChannelHandler;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import de.kseek.core.executor.XWorkExecutor;
import de.kseek.core.listener.SessionReferenceBinder;
import de.kseek.core.net.Connect;
import de.kseek.core.net.NetAddress;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.Map;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Slf4j
@ChannelHandler.Sharable
public class ClusterMessageDispatcher implements ApplicationListener<ContextRefreshedEvent> {

    private Map<Integer, MessageController> messageControllers;

    /**
     * -- SETTER --
     *  设置 ClusterSystem 实例
     *  用于避免循环依赖，通过 setter 注入而不是构造函数注入
     *
     * @param clusterSystem ClusterSystem 实例
     */
    @Setter
    private ClusterSystem clusterSystem;
    
    @Autowired(required = false)
    public SessionReferenceBinder sessionReferenceBinder;

    @Autowired(required = false)
    public XWorkExecutor xWorkExecutor;

    /**
     * 扫描处理器包
     * -- SETTER --
     *  设置扫描处理器包
     *
     * @param pkgs 包名数组

     */
    @Setter
    private String[] pkgs;

    public PFSession getPFSession(String id) {
        return clusterSystem.sessionMap().get(id);
    }

    public void onClusterReceive(Connect connect, ClusterMessage clusterMessage) {
        String sessionId = clusterMessage.sessionId;
        PFSession session = null;
        if (!StringUtils.isBlank(sessionId)) {
            session = getPFSession(sessionId);
            if (session != null) {
                session.activeTime = System.currentTimeMillis();
            } else if (sessionReferenceBinder != null) {
                session = new PFSession(sessionId, connect, new NetAddress("127.0.0.1", 30021));
                session.userId = clusterMessage.userId;
                sessionReferenceBinder.bind(session, clusterMessage.userId);
            }
        }
        PFMessage msg = clusterMessage.msg;
        try {
            final PFSession pfSession = session;
            if (session != null && session.getTaskGroup() != null) {
                // 绑定任务组
                session.getTaskGroup().submit(() -> handle(connect, pfSession, msg));
            } else if (session != null && session.workId > 0 && xWorkExecutor != null) {
                // 此处如果用户有工作ID，采用工作线程提交
                xWorkExecutor.submit(session.workId, () -> handle(connect, pfSession, msg));
            } else {
                handle(connect, session, msg);
            }
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    public void handle(Connect connect, PFSession session, PFMessage msg) {
        int messageType = 0;
        int command = 0;
        try {
            messageType = msg.messageType;
            command = msg.cmd;
            MessageController messageController = messageControllers.get(messageType);
            Object response = null;
            if (messageController != null) {
                MethodInfo methodInfo = messageController.MethodInfos.get(command);
                if (methodInfo == null) {
                    log.warn("找不到处理函数,messageType={},cmd={}", messageType, command);
                    return;
                }
                Object bean = messageController.been;
                if (methodInfo.parms != null && methodInfo.parms.length > 0) {
                    Object[] args = new Object[methodInfo.parms.length];
                    Object reference = null;
                    if (session != null) {
                        reference = session.getReference();
                        MDC.put("player-id", session.getUserId() + "");
                    }
                    for (int i = 0; i < args.length; i++) {
                        Class<?> clazz = methodInfo.parms[i];
                        if (clazz == PFSession.class) {
                            args[i] = session;
                        } else if (reference != null && clazz == reference.getClass()) {
                            args[i] = reference;
                        } else if (Connect.class.isAssignableFrom(clazz)) {
                            args[i] = connect;
                        } else {
                            if (msg.data != null && msg.data.length > 0) {
                                args[i] = ProtostuffUtil.deserialize(msg.data, clazz);
                            }
                        }
                    }
                    response = messageController.methodAccess.invoke(bean, methodInfo.index, args);
                } else {
                    response = messageController.methodAccess.invoke(bean, methodInfo.index);
                }
            } else {
                log.warn("未被注册的消息,messageType={},cmd={}", messageType, command);
            }
            // 消息响应
            if (response != null && session != null) {
                session.response(msg.msgId, response);
            }
        } catch (Exception e) {
            log.warn("消息解析错误,messageType={},cmd={}", messageType, command, e);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        messageControllers = MessageUtil.load(event.getApplicationContext());
        messageControllers.forEach((key, value) -> log.info("消息处理器[{}]->{}", key, value.been.getClass().getName()));
        log.info("加载返回消息注解,pkgs={}", pkgs == null ? "" : StrUtil.join(",", Arrays.asList(pkgs)));
        MessageUtil.loadResponseMessage(pkgs);
    }
}
