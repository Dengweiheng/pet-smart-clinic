package com.server.server.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.server.server.config.AiProperties;
import com.server.server.dto.HealthConsultationRequest;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI 宠物智能健康咨询服务类
 * <p>
 * 基于阿里通义千问 (DashScope) 大模型与 SSE (Server-Sent Events) 技术，
 * 实现流式输出宠物健康问诊建议、用药指导与家庭护理方案。
 */
@Service
@RequiredArgsConstructor
public class HealthConsultationService {

    private final AiProperties aiProperties;

    /**
     * 开启流式 AI 宠物健康咨询
     *
     * @param request 包含宠物基础档案、当前症状描述及历史会话上下文的请求对象
     * @return 用于与前端建立 SSE 单向长连接的 SseEmitter
     */
    public SseEmitter streamConsultation(HealthConsultationRequest request) {
        // 设置 0L 表示长连接永不超时，直到会话结束
        SseEmitter emitter = new SseEmitter(0L);
        // 异步线程执行大模型调用与推流，避免阻塞 Web 容器主线程
        CompletableFuture.runAsync(() -> doStream(request, emitter));
        return emitter;
    }

    /**
     * 执行大模型流式调用与 SSE 数据推送核心逻辑
     *
     * @param request 问诊请求
     * @param emitter SSE 发送端
     */
    private void doStream(HealthConsultationRequest request, SseEmitter emitter) {
        try {
            Generation gen = new Generation();
            
            // 构建 System 角色 Prompt，明确诊断规范、回答框架与边界约束
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("你是宠物健康咨询助手。请基于用户提供的信息给出初步建议，必须包含紧急程度判断、可能病因、家庭护理及推荐非处方药。最后附带免责声明。")
                    .build();
            
            // 构建 User 角色 Prompt，组装宠物信息与历史对话
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(buildPrompt(request))
                    .build();

            // 配置大模型调用参数，开启增量流式输出 (incrementalOutput)
            GenerationParam param = GenerationParam.builder()
                    .apiKey(aiProperties.getApiKey())
                    .model(aiProperties.getModel())
                    .messages(List.of(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .incrementalOutput(true) // 关键：增量输出支持流式 SSE
                    .build();

            // 订阅流式返回结果并通过 SSE 逐字/逐词推送给前端
            Flowable<GenerationResult> result = gen.streamCall(param);
            
            result.blockingForEach(message -> {
                String text = message.getOutput().getChoices().get(0).getMessage().getContent();
                if (text != null) {
                    sendData(emitter, text);
                }
            });

            // 附带固定的医疗免责安全声明
            sendData(emitter, "\n\n【安全温馨提示】以上 AI 建议仅供学术参考，不作为正式医疗诊断依据。若宠物状态急剧恶化，请务必立即送医。");
            emitter.complete();
        } catch (Exception e) {
            e.printStackTrace();
            emitter.completeWithError(e);
        }
    }

    /**
     * 组装结构化 AI 提示词 (Prompt)
     *
     * @param request 问诊请求
     * @return 完整的 Prompt 文本
     */
    private String buildPrompt(HealthConsultationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是宠物健康咨询助手。请基于用户提供的信息给出初步建议，必须包含：\n")
                .append("1) 紧急程度判断（低/中/高）\n")
                .append("2) 可能病因分析（最多3条）\n")
                .append("3) 家庭护理建议\n")
                .append("4) 推荐非处方药列表（每条含药名、用途、注意事项）\n")
                .append("5) 如需立即就医，给出明确指引\n")
                .append("6) 最后必须附带免责声明\n")
                .append("7) 若提到推荐药物，请额外附上购买链接，链接必须使用以下前缀并拼接 URL 编码后的药名：")
                .append(buildDrugLinkPrefix())
                .append("{药名}\n\n")
                .append("宠物信息：\n")
                .append("- 宠物名：").append(nullToEmpty(request.getPetName())).append("\n")
                .append("- 物种：").append(nullToEmpty(request.getSpecies())).append("\n")
                .append("- 品种：").append(nullToEmpty(request.getBreed())).append("\n")
                .append("- 年龄：").append(nullToEmpty(request.getAge())).append("\n")
                .append("- 当前症状：").append(nullToEmpty(request.getSymptoms())).append("\n\n")
                .append("历史对话：\n")
                .append(joinHistory(request.getHistory()));

        return prompt.toString();
    }

    /**
     * 拼接历史多轮问诊记录
     *
     * @param history 历史消息列表
     * @return 格式化后的历史对话文本
     */
    private String joinHistory(List<HealthConsultationRequest.HistoryMessage> history) {
        if (history == null || history.isEmpty()) {
            return "无";
        }
        List<String> lines = new ArrayList<>();
        for (HealthConsultationRequest.HistoryMessage item : history) {
            lines.add("[" + nullToEmpty(item.getRole()) + "] " + nullToEmpty(item.getContent()));
        }
        return String.join("\n", lines);
    }

    /**
     * 获取推荐药品的跳转链接基础前缀（去除末尾斜杠）
     *
     * @return 药品链接前缀
     */
    private String buildDrugLinkPrefix() {
        return aiProperties.getDrugDetailBaseUrl().endsWith("/")
                ? aiProperties.getDrugDetailBaseUrl().substring(0, aiProperties.getDrugDetailBaseUrl().length() - 1)
                : aiProperties.getDrugDetailBaseUrl();
    }

    /**
     * 空值转空字符串工具方法
     */
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * 向 SSE 连接通道发送数据片段
     *
     * @param emitter SSE 实例
     * @param chunk 数据块
     */
    private void sendData(SseEmitter emitter, String chunk) {
        try {
            emitter.send(SseEmitter.event().name("message").data(chunk));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

