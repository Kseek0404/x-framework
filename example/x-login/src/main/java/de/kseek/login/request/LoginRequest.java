package de.kseek.login.request;

import lombok.Data;

@Data
public class LoginRequest {
    /**
     *  登录用户名
     */
    private String account;

    /**
     *  密码
     */
    private String pwd;
}
