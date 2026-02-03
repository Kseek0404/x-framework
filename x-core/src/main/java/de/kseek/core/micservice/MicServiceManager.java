package de.kseek.core.micservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import de.kseek.core.protostuff.MessageType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Component
@Slf4j
public class MicServiceManager implements ApplicationListener<ContextRefreshedEvent> {
    public Set<Integer> messageTypes = new HashSet<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        scanAndRegister(applicationContext);
    }

    public void scanAndRegister(ApplicationContext context) {
        Class<MicService> clazz = MicService.class;
        log.debug("开始扫描 {} 微服务", clazz);
        Map<String, Object> beans = context.getBeansWithAnnotation(clazz);
        beans.values().forEach(o -> {
            MessageType messageType = null;
            if (AopUtils.isAopProxy(o)) {
                messageType = AopUtils.getTargetClass(o).getAnnotation(MessageType.class);
            } else {
                messageType = o.getClass().getAnnotation(MessageType.class);
            }
            if (messageType == null) {
                log.debug("未被 MessageType 注解的微服务,{}", o.getClass());
            } else {
                log.debug("扫描到微服务,messageType={},class={}", messageType.value(), o.getClass());
                messageTypes.add(messageType.value());
            }
        });
    }
}
