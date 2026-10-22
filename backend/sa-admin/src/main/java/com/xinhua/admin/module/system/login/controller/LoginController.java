package com.xinhua.admin.module.system.login.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.xinhua.admin.constant.AdminSwaggerTagConst;
import com.xinhua.admin.module.system.login.domain.LoginForm;
import com.xinhua.admin.module.system.login.domain.LoginResultVO;
import com.xinhua.admin.module.system.login.service.LoginService;
import com.xinhua.admin.util.AdminRequestUtil;
import com.xinhua.base.common.annoation.NoNeedLogin;
import com.xinhua.base.common.constant.RequestHeaderConst;
import com.xinhua.base.common.domain.ResponseDTO;
import com.xinhua.base.common.util.SmartRequestUtil;
import com.xinhua.base.module.support.captcha.domain.CaptchaVO;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = AdminSwaggerTagConst.System.SYSTEM_LOGIN)
public class LoginController {

    @Resource
    private LoginService loginService;

    @NoNeedLogin
    @PostMapping("/login")
    @Operation(summary = "登录 @author 卓大")
    public ResponseDTO<LoginResultVO> login(@Valid @RequestBody LoginForm loginForm, HttpServletRequest request) {
        String ip = JakartaServletUtil.getClientIP(request);
        String userAgent = JakartaServletUtil.getHeaderIgnoreCase(request, RequestHeaderConst.USER_AGENT);
        return loginService.login(loginForm, ip, userAgent);
    }

    @GetMapping("/login/getLoginInfo")
    @Operation(summary = "获取登录结果信息  @author 卓大")
    public ResponseDTO<LoginResultVO> getLoginInfo() {
        String tokenValue = StpUtil.getTokenValue();
        LoginResultVO loginResult = loginService.getLoginResult(AdminRequestUtil.getRequestUser(), tokenValue);
        loginResult.setToken(tokenValue);
        return ResponseDTO.ok(loginResult);
    }

    @Operation(summary = "退出登录  @author 卓大")
    @GetMapping("/login/logout")
    public ResponseDTO<String> logout() {
        return loginService.logout(SmartRequestUtil.getRequestUser());
    }

    @Operation(summary = "获取验证码  @author 卓大")
    @GetMapping("/login/getCaptcha")
    @NoNeedLogin
    public ResponseDTO<CaptchaVO> getCaptcha() {
        return loginService.getCaptcha();
    }

}
