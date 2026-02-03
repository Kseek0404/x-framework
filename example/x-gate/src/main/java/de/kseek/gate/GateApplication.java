package de.kseek.gate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 网关服务启动类 - 独立可运行。
 * TimerCenter 由 x-core 的 TimerCenterConfiguration 统一提供，可通过 x.timer.* 配置。
 */
@SpringBootApplication
@ComponentScan(basePackages = {"de.kseek.core", "de.kseek.gate"})
public class GateApplication {

    public static void main(String[] args) {
        SpringApplication.run(GateApplication.class, args);
    }
}
