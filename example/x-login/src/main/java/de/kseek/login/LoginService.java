package de.kseek.login;

import de.kseek.core.protostuff.Command;
import de.kseek.core.protostuff.MessageType;
import de.kseek.core.protostuff.PFSession;
import de.kseek.game.common.GameMessageConst;
import de.kseek.game.common.message.ReqLogin;
import de.kseek.game.common.message.RespLogin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 登录微服务 - 处理 messageType=1000
 */
@Slf4j
@Component
@MessageType(GameMessageConst.LOGIN_TYPE)
public class LoginService {

    /**
     * 登录请求（示例：账号 test/123456 通过）
     */
    @Command(GameMessageConst.LOGIN_REQ)
    public RespLogin login(PFSession session, ReqLogin req) {
        RespLogin resp = new RespLogin();
        if (req == null || req.getAccount() == null || req.getPassword() == null) {
            resp.setSuccess(false);
            resp.setMessage("账号或密码为空");
            return resp;
        }
        if ("test".equals(req.getAccount()) && "123456".equals(req.getPassword())) {
            long userId = 10001L;
            resp.setSuccess(true);
            resp.setUserId(userId);
            resp.setMessage("登录成功");
            // 通知 Gate 认证通过
            session.verifyPass(userId, session.getAddress() != null ? session.getAddress().getHost() : "127.0.0.1", null);
        } else {
            resp.setSuccess(false);
            resp.setMessage("账号或密码错误");
        }
        return resp;
    }
}
