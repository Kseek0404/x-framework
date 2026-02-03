package de.kseek.core;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
/**
 * @author kseek
 * @date 2024/3/22
 */
@Component
public final class XContext implements ApplicationContextAware {

    static Map<String, Object> xBeans = new HashMap<>();

    static ApplicationContext CONTEXT;

    // 获取applicationContext
    public static ApplicationContext getContext() {
        return CONTEXT;
    }

    public static <T> T getXBean(Class<T> clazz) {
        return (T) xBeans.get(clazz.getSimpleName());
    }

    public static void addXBean(Object obj) {
        xBeans.put(obj.getClass().getSimpleName(), obj);
    }

    // 通过name获取 Bean.
    public static Object getBean(String name) {
        return getContext().getBean(name);
    }

    // 通过class获取Bean.
    public static <T> T getBean(Class<T> clazz) {
        return getContext().getBean(clazz);
    }

    // 通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getContext().getBean(name, clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
    }
}
