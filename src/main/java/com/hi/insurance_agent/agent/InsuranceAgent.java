package com.hi.insurance_agent.agent;

import com.hi.insurance_agent.advisor.LogAdvisor;
import com.hi.insurance_agent.rag.local_rag.RagCustomAdvisorFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class InsuranceAgent extends ToolCallAgent {
    final JdbcTemplate jdbc;
    final EmbeddingModel embeddingModel;

    public InsuranceAgent(
            ToolCallback[] allTools,
            ChatModel dashscopeChatModel,
            JdbcTemplate jdbc,
            EmbeddingModel embeddingModel
    ) {
        super(allTools);
        this.jdbc = jdbc;
        this.embeddingModel = embeddingModel;

        this.setName("Insurance Agent");

        String SYSTEM_PROMPT = """
                You are Insurance Agent, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);

        String NEXT_STEP_PROMPT = """
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(10);

        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(
                        new LogAdvisor(),
                        RagCustomAdvisorFactory.createHybridRagAdvisor(jdbc, embeddingModel)
                )
                .build();
        this.setChatClient(chatClient);

    }

}