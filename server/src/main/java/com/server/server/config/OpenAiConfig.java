package com.server.server.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 服务配置类
 * <p>
 * 启用 {@link AiProperties} 配置属性绑定，为 AI 问诊模块提供参数支持。
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class OpenAiConfig {
    // 问诊大模型已接入阿里通义千问 DashScope SDK
}

