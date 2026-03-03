package com.hi.insurance_agent.app;

import com.hi.insurance_agent.advisor.LogAdvisor;
import com.hi.insurance_agent.tools.RewriteQueryTool;
import com.hi.insurance_agent.rag.local_rag.RagCustomAdvisorFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Slf4j
public class InsuranceApp {

    private final RewriteQueryTool rewriteQueryTool;

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "你是一个保险领域查询助手，请根据用户的问题，给出相应的回答";

    public InsuranceApp(ChatModel chatModel) {
        // 创建查询重写工具，直接使用 rewriteQuery 方法进行改写
        this.rewriteQueryTool = new RewriteQueryTool(chatModel);

        chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new LogAdvisor()
                )
                .build();
    }

    // 规范化口语，直接调用 rewriteQuery 方法
    public String normalizeMessage(String message) {
        return rewriteQueryTool.rewriteQuery(message);
    }

    // 实际查询
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                // 注入参数，包括对话id和上下文记忆长度.  传入对话id作为保存文件名
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId)
                        .param("chat_memory_retrieve_size_key", 10))
                .advisors(new LogAdvisor())
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        return content;
    }

    @Resource
    JdbcTemplate jdbc;

    @Resource
    EmbeddingModel embeddingModel;

    public String doChatWithRag(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                // 注入参数，包括对话id和上下文记忆长度.  传入对话id作为保存文件名
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId)
                        .param("chat_memory_retrieve_size_key", 10))
                .advisors(RagCustomAdvisorFactory.createHybridRagAdvisor(jdbc, embeddingModel))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        return content;
    }

    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId)
                        .param("chat_memory_retrieve_size_key", 10))
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        return content;
    }

    public String doChatWithRagAndTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId)
                        .param("chat_memory_retrieve_size_key", 10))
                .advisors(RagCustomAdvisorFactory.createHybridRagAdvisor(jdbc, embeddingModel))
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        return content;
    }

    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId)
                        .param("chat_memory_retrieve_size_key", 10))
                .advisors(RagCustomAdvisorFactory.createHybridRagAdvisor(jdbc, embeddingModel))
                .toolCallbacks(allTools)
                .stream()
                .content();
    }

    // 读取规范化口语的 prompt 文件
    public static String readFile(File insurancePromptFile) {
        String systemPrompt;
        try {
            systemPrompt = Files.readString(Path.of(insurancePromptFile.getPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file", e);
        }
        return systemPrompt;
    }
    
    private String cleanNormalized(String raw) {
        if (raw == null) {
            return "";
        }
        String text = raw.trim();
        if (text.startsWith("```") && text.endsWith("```")) {
            text = text.substring(3, text.length() - 3).trim();
        }
        String[][] quotes = new String[][]{
                {"\"", "\""},
                {"'", "'"},
                {"“", "”"},
                {"‘", "’"},
                {"「", "」"},
                {"『", "』"}
        };
        for (String[] pair : quotes) {
            if (text.startsWith(pair[0]) && text.endsWith(pair[1]) && text.length() >= 2) {
                text = text.substring(pair[0].length(), text.length() - pair[1].length()).trim();
                break;
            }
        }
        text = text.replaceAll("\r\n", "\n");
        text = text.replaceAll("\n+", " ");
        text = text.replaceAll("\s+", " ").trim();
        return text;
    }

}

