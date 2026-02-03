package de.kseek.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 登录服务启动类 - 独立可运行
 */
@SpringBootApplication
@ComponentScan(basePackages = {"de.kseek.core", "de.kseek.login"})
public class LoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoginApplication.class, args);
    }
}
