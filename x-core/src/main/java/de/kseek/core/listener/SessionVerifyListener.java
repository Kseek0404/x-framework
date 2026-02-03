package de.kseek.core.listener;

/**
 * @author kseek
 * @date 2024/3/22
 */
public interface SessionVerifyListener {
    void userVerifyPass(String sessionId, long userId,String ip);
}
