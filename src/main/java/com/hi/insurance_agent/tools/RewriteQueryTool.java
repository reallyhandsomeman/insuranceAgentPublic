package com.hi.insurance_agent.tools;

import com.hi.insurance_agent.app.InsuranceApp;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

/**
 * 查询重写工具
 * 使用 Spring AI 的 RewriteQueryTransformer 来重写用户查询，
 * 使其更适合在向量存储或搜索引擎中查询
 * 使用 insurance_system_prompt.txt 中的内容作为自定义 PromptTemplate
 */
public class RewriteQueryTool {
    
    private final QueryTransformer queryTransformer;
    
    public RewriteQueryTool(ChatModel chatModel) {
        // 读取 insurance_system_prompt.txt 文件内容
        String promptTemplateContent = InsuranceApp.readFile(
            new File("./src/main/java/com/hi/insurance_agent/constant/insurance_system_prompt.txt")
        );
        
        // 构建完整的提示词模板
        String fullPromptTemplate = promptTemplateContent + 
            "\n\n你的任务是将用户查询改写为更适合在向量存储或搜索引擎中查询的规范化查询。" +
            "改写后的查询应该使用标准的保险行业术语，确保语义不变、表述规范。" +
            "如果查询已经很规范，可以直接返回原查询。" +
            "\n\n请将以下查询改写为更适合搜索的规范化查询：\n{query}\n{target}";
        
        // 创建自定义的 PromptTemplate
        PromptTemplate promptTemplate = new PromptTemplate(fullPromptTemplate);
        
        // 创建 ChatClient.Builder 用于 RewriteQueryTransformer
        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);
        
        // 构建 RewriteQueryTransformer，使用自定义的 PromptTemplate
        this.queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .promptTemplate(promptTemplate)
                .build();
    }
    
    /**
     * 重写查询工具
     * 将用户查询改写为更适合在向量存储或搜索引擎中查询的规范化查询
     * 
     * @param userQuery 用户原始查询
     * @return 改写后的规范化查询
     */
    @Tool(description = """
            查询重写工具（必须优先使用）。使用大语言模型将用户查询改写为更适合在向量存储或搜索引擎中查询的规范化查询。
            
            使用场景：
            - 用户查询包含口语化、非正式的保险表达（如"年年都有钱拿的保险"、"能返本的保险"等）
            - 用户查询包含俗称、简称、模糊说法、错别字或拼音
            - 需要将用户查询转换为标准的保险行业术语
            
            此工具会将口语表达映射为标准的保险行业术语与正式问法，确保语义不变、表述规范。
            改写后的查询更适合进行向量搜索和后续处理。
            
            注意：在处理任何用户查询时，应该优先使用此工具进行规范化，然后再使用其他工具进行查询。
            """)
    public String rewriteQuery(@ToolParam(description = "需要重写的用户原始查询") String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return userQuery;
        }
        
        try {
            // 创建 Query 对象
            Query query = new Query(userQuery);
            
            // 使用 RewriteQueryTransformer 转换查询
            Query transformedQuery = queryTransformer.transform(query);
            
            // 返回改写后的查询文本（Query 是 Record 类，使用 text() 方法）
            return transformedQuery.text();
        } catch (Exception e) {
            // 如果转换失败，返回原始查询
            return userQuery;
        }
    }
}

