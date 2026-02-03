package de.kseek.core.listener;

import de.kseek.core.protostuff.PFSession;

/**
 * @author kseek
 * @date 2024/3/22
 */
public interface SessionReferenceBinder {
    Object bind(PFSession session, long userId);
}
