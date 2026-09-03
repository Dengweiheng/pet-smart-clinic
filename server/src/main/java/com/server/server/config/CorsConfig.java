package com.server.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 全局跨域资源共享 (CORS) 过滤器配置类
 * <p>
 * 解决前端（Vue/React）与后端 Spring Boot 之间前后端分离部署时的跨域访问限制问题。
 */
@Configuration
public class CorsConfig {

    /**
     * 注册跨域过滤器 Bean
     *
     * @return CorsFilter 跨域过滤器实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源域名进行跨域请求
        config.setAllowedOriginPatterns(List.of("*"));
        // 允许常见的 HTTP 动词
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许携带所有请求头（例如 Authorization, Content-Type 等）
        config.setAllowedHeaders(List.of("*"));
        // 是否允许携带凭据 (Cookie 等)
        config.setAllowCredentials(false);

        // 对所有请求路径 "/**" 应用上述跨域策略
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

