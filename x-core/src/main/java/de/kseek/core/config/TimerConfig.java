package de.kseek.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 定时器配置，用于 TimerCenter Bean 的创建与命名。
 * 可通过 x.timer.* 在 application.yml 中配置。
 *
 * @author kseek
 */
@ConfigurationProperties(prefix = "x.timer")
@Getter
@Setter
public class TimerConfig {

    /**
     * 是否启用 TimerCenter Bean，默认 true。
     * 设为 false 时 ClusterSystem 等将得到 Optional.empty()。
     */
    private boolean enabled = true;

    /**
     * 定时器线程名称，便于日志与线程栈区分，默认 "timer"。
     */
    private String name = "timer";
}
