package de.kseek.core.protostuff;


import com.esotericsoftware.reflectasm.MethodAccess;

import java.util.Map;

/**
 * @author kseek
 * @date 2024/3/22
 */
public class MessageController {
    public Object been;
    public MethodAccess methodAccess;
    public Map<Integer, MethodInfo> MethodInfos;

    public MessageController(Object been) {
        this.been = been;
        Class<?> clazz = been.getClass();
        methodAccess = MethodAccess.get(clazz);
        MethodInfos = MessageUtil.load(methodAccess, clazz);
    }

    public MessageController(Object been, Class<?> clazz) {
        this.been = been;
        //Class<?> clazz = been.getClass();
        methodAccess = MethodAccess.get(clazz);
        MethodInfos = MessageUtil.load(methodAccess, clazz);
    }
}
