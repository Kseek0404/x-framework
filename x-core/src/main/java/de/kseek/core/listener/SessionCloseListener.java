package de.kseek.core.listener;

import de.kseek.core.protostuff.PFSession;

/**
 * @author kseek
 * @date 2024/3/22
 */
public interface SessionCloseListener {
    void sessionClose(PFSession session);
}
