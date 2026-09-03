package com.server.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置类
 * <p>
 * 配置平台的访问控制策略。当前系统基于自定义 Token 在服务层进行鉴权控制，
 * 此处放行全部 HTTP 请求并禁用 CSRF，同时启用默认跨域配置。
 */
@Configuration
public class SecurityConfig {

    /**
     * 配置安全过滤链
     *
     * @param http HttpSecurity 对象
     * @return SecurityFilterChain 实例
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（跨站请求伪造）保护，适配前后端分离 API 交互
                .csrf(csrf -> csrf.disable())
                // 启用默认 CORS 配置（结合 CorsConfig）
                .cors(Customizer.withDefaults())
                // 放行所有 HTTP 请求路由
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}

