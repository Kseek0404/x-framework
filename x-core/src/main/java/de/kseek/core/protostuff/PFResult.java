package de.kseek.core.protostuff;

import lombok.Data;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Data
public class PFResult {
    /* 返回码*/
    private long code;
    /* 返回消息*/
    private Object msg;
    /* 消息类型*/
    private Class<?> clazz;

    public PFResult() {
    }

    public PFResult(long code, Object msg, Class<?> clazz) {
        this.code = code;
        this.msg = msg;
        this.clazz = clazz;
    }

    public PFResult(long code, Object msg) {
        this.code = code;
        this.msg = msg;
    }

    public PFResult(long code, Class<?> clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    public static PFResult build(long code, Object msg, Class<?> clazz) {
        return new PFResult(code, msg, clazz);
    }

    public static PFResult build(long code, Object msg) {
        return new PFResult(code, msg, null);
    }

    public static PFResult build(long code, Class<?> clazz) {
        return new PFResult(code, null, clazz);
    }
}
