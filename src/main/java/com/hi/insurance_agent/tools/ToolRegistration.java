package com.hi.insurance_agent.tools;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ToolRegistration {

    @Autowired
    private ChatModel chatModel;

    @Bean
    public ToolCallback[] allTools() {
        ReadRatePdfTool readRatePdfTool = new ReadRatePdfTool();
        TerminateTool terminateTool = new TerminateTool();
        GetAllInsuranceTool getAllInsuranceTool = new GetAllInsuranceTool();
        ReadMarkdownTool readMarkdownTool = new ReadMarkdownTool();
        RewriteQueryTool rewriteQueryTool = new RewriteQueryTool(chatModel);
        return ToolCallbacks.from(
                readRatePdfTool,
                terminateTool,
                getAllInsuranceTool,
                rewriteQueryTool,
                readMarkdownTool
        );
    }
}
