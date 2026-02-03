package de.kseek.core.net;

/**
 * @author kseek
 * @date 2024/3/22
 */
public interface SessionListener {
    /**
     * 当回话被关闭
     *
     * @param session
     */
    void onSessionClose(Session session);
}
