package de.kseek.gate;

import de.kseek.core.gate.GateSession;
import de.kseek.core.listener.SessionVerifyListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 认证通过后更新 GateSession 的 certify 与 userId
 */
@Slf4j
@Component
public class GateSessionVerifyListener implements SessionVerifyListener {

    @Override
    public void userVerifyPass(String sessionId, long userId, String ip) {
        GateSession gateSession = GateSession.gateSessionMap.get(sessionId);
        if (gateSession != null) {
            gateSession.certify = true;
            gateSession.userId = userId;
            log.info("会话认证通过 sessionId={}, userId={}", sessionId, userId);
        } else {
            log.warn("认证通过但找不到 GateSession sessionId={}", sessionId);
        }
    }
}
