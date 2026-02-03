package de.kseek.hall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 大厅服务启动类 - 独立可运行
 */
@SpringBootApplication
@ComponentScan(basePackages = {"de.kseek.core", "de.kseek.hall"})
public class HallApplication {

    public static void main(String[] args) {
        SpringApplication.run(HallApplication.class, args);
    }
}
