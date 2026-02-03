package de.kseek.core.listener;

/**
 * @author kseek
 * @date 2024/3/22
 */
public interface SessionLogoutListener {
    void logout(long playerId, String sessionId);
}
