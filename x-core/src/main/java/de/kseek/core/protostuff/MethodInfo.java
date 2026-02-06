package de.kseek.core.protostuff;

import java.lang.reflect.Type;

/**
 * @author kseek
 * @date 2024/3/22
 * @param <T> 方法返回值类型
 */
public class MethodInfo<T> {
    public int index;
    public String name;
    public Class<?>[] parms;
    public Type returnType;

    public MethodInfo(int index, String name, Class<?>[] parms, Type returnType) {
        this.index = index;
        this.name = name;
        this.parms = parms;
        this.returnType = returnType;
    }
}
