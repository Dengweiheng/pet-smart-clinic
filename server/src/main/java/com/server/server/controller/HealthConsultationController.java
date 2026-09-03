package com.server.server.controller;

import com.server.server.dto.HealthConsultationRequest;
import com.server.server.service.HealthConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 宠物智能健康问诊控制器
 * <p>
 * 提供基于 SSE (Server-Sent Events) 的流式问诊会话接口，
 * 实时将大语言模型生成的病情分析、用药指导和就医建议推送到客户端。
 */
@RestController
@RequestMapping("/api/consultation")
@RequiredArgsConstructor
public class HealthConsultationController {

    private final HealthConsultationService healthConsultationService;

    /**
     * AI 流式健康问诊接口
     * <p>
     * 接口通过 `text/event-stream` 媒体类型建立 SSE 长连接，逐字推送流式问诊分析结果。
     *
     * @param request 包含宠物档案、主要症状描述和历史问诊消息的请求体
     * @return SseEmitter 实例，负责向前端实时推流
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody HealthConsultationRequest request) {
        return healthConsultationService.streamConsultation(request);
    }
}

