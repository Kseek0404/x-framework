package de.kseek.login.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 用户实体 - MongoDB 文档
 */
@Data
@Document(collection = "user")
public class Account {

    @Id
    private String id;

    /**
     * 用户唯一 ID（业务用）
     */
    private Long userId;

    /**
     * 登录账号
     */
    @Indexed(unique = true)
    private String account;

    /**
     * 密码（示例明文，生产建议加密存储）
     */
    private String pwd;

    /**
     * 创建时间
     */
    private Date createTime;
}
