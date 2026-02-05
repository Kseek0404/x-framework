package de.kseek.login.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private String gateAddress;

    private long userId;
}
