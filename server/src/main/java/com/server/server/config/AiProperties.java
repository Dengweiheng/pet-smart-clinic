package com.server.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 大模型配置属性类
 * <p>
 * 映射 application.yml 中以 `ai` 为前缀的相关配置项，
 * 用于支持通义千问 (DashScope) 智能问诊服务及药品链接构建。
 */
@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * 阿里云 DashScope / 通义千问 API 密钥 (API-KEY)
     */
    private String apiKey;

    /**
     * 调用的 AI 模型名称，默认为通义千问 "qwen-plus"
     */
    private String model = "qwen-plus";

    /**
     * 药品详情页的前端基础跳转地址，用于 AI 诊断建议中动态拼接推荐药品的直达链接
     */
    private String drugDetailBaseUrl = "http://localhost:5173/drugs";
}

