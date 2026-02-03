package de.kseek.core.timer;

import de.kseek.core.config.TimerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 提供 TimerCenter Bean，供 ClusterSystem、ConnectPool 等通过 Optional&lt;TimerCenter&gt; 注入。
 * 由框架统一创建与销毁，应用侧无需再手写 @Bean。
 *
 * @author kseek
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(TimerConfig.class)
@ConditionalOnProperty(prefix = "x.timer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TimerCenterConfiguration {

    @Bean(destroyMethod = "close")
    public TimerCenter timerCenter(TimerConfig config) {
        String name = config.getName() != null ? config.getName() : "timer";
        TimerCenter center = new TimerCenter(name);
        center.start();
        log.debug("TimerCenter started, name={}", name);
        return center;
    }
}
