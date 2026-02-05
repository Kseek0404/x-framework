package de.kseek.login.controller;

import de.kseek.core.web.R;
import de.kseek.login.request.LoginRequest;
import de.kseek.login.service.LoginService;
import de.kseek.login.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/login")
    public R<LoginVO> testLogin(HttpServletRequest request, @RequestBody LoginRequest loginRequest) {
        LoginVO authResult = loginService.login(request.getRemoteHost(), loginRequest);
        if (authResult == null) {
            return R.fail("账号或密码错误");
        }
        return R.ok("账号认证成功", authResult);
    }
}
