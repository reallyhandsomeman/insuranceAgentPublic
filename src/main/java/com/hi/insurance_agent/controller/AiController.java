package com.hi.insurance_agent.controller;

import com.hi.insurance_agent.agent.InsuranceAgent;
import com.hi.insurance_agent.app.InsuranceApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private InsuranceApp insuranceApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    JdbcTemplate jdbc;

    @Resource
    EmbeddingModel embeddingModel;

    /**
     * 以完整response返回方式，与智能体交流
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/ask/chat/sync")
    public String doChatWithRentAppSync(String message, String chatId) {
        return insuranceApp.doChat(message, chatId);
    }

    // 以SSE返回
    @GetMapping(value = "/ask/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithRentAppSSE(String message, String chatId) {
        return insuranceApp.doChatByStream(message, chatId);
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/agent/chat")
    public SseEmitter doChatWithAgent(String message) {
        InsuranceAgent insuranceAgent = new InsuranceAgent(allTools, dashscopeChatModel, jdbc, embeddingModel);
        return insuranceAgent.runStream(message);
    }
}