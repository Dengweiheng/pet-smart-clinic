package com.server.server.controller;

import com.server.server.dto.LoginRequest;
import com.server.server.dto.LoginResponse;
import com.server.server.dto.LogoutRequest;
import com.server.server.dto.RegisterRequest;
import com.server.server.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户认证与权限管理控制器
 * <p>
 * 提供系统的登录、注册以及退出登录等安全交互接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录接口
     *
     * @param request 登录入参（包含用户名、密码）
     * @return 包含 Token、用户 ID、身份角色等信息的登录响应
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * 新用户注册接口
     *
     * @param request 注册入参（包含用户名、密码、手机号、角色等）
     * @return 操作结果状态
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return Map.of("success", true);
    }

    /**
     * 用户注销/退出登录接口
     *
     * @param request 退出登录请求（包含 Token）
     * @return 操作结果状态
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestBody LogoutRequest request) {
        boolean success = authService.logout(request.getToken());
        return Map.of("success", success);
    }
}

