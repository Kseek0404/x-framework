package de.kseek.login.service;

import de.kseek.core.config.NodeConfig;
import de.kseek.core.curator.NodeType;
import de.kseek.core.curator.XNode;
import de.kseek.game.common.manager.GameNodeManager;
import de.kseek.login.entity.Account;
import de.kseek.login.repository.AccountRepository;
import de.kseek.login.request.LoginRequest;
import de.kseek.login.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * 登录业务 - HTTP 接口用
 */
@Service
@RequiredArgsConstructor
public class LoginService {

    private final AccountRepository accountRepository;

    private final GameNodeManager gameNodeManager;

    /**
     * 账号密码登录，成功返回 LoginVO，失败返回 null
     */
    public LoginVO login(String ip, LoginRequest loginRequest) {
        if (loginRequest == null || StringUtils.isBlank(loginRequest.getAccount()) || StringUtils.isBlank(loginRequest.getPwd())) {
            return null;
        }
        Optional<Account> opt = accountRepository.findByAccount(loginRequest.getAccount().trim());
        if (!opt.isPresent()) {
            return null;
        }
        Account account = opt.get();
        if (!loginRequest.getPwd().equals(account.getPwd())) {
            return null;
        }
        LoginVO vo = new LoginVO();
        vo.setUserId(account.getUserId());
        XNode gateNode = gameNodeManager.getGateNode(NodeType.GATE, ip);
        NodeConfig nodeConfig = gateNode.getNodeConfig();
        vo.setGateAddress(StringUtils.isNotBlank(nodeConfig.getTcpStrAddress()) ? nodeConfig.getTcpStrAddress() : nodeConfig.getWssStrAddress());
        vo.setToken(UUID.randomUUID().toString());
        return vo;
    }
}
