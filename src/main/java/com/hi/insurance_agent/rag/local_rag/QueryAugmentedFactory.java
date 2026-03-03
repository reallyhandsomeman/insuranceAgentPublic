package com.hi.insurance_agent.rag.local_rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

import java.util.stream.Collectors;

public class QueryAugmentedFactory {

    /**
     * 没有检索到任何文档，按模版回复用户。把metadata一起转换为字符串贴进prompt
     *
     * @return ContextualQueryAugmenter
     */
    public static ContextualQueryAugmenter createInstance() {
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答利安保险相关的问题，别的没办法帮到您哦。
                """);
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .documentFormatter(documents -> documents.stream()
                        .map(d -> {
                            var meta = d.getMetadata();
                            return "Text: " + d.getText() + System.lineSeparator() +
                                    "Metadata -> " +
                                    "title: " + meta.get("title") + ", " +
                                    "category: " + meta.get("category") + ", " +
                                    "effective_date: " + meta.get("effective_date") + ", " +
                                    "abolition_statement: " + meta.get("abolition_statement") + ", " +
                                    "File path: " + meta.get("path") + ", " +
                                    "esg: " + meta.get("esg");
                        })
                        .collect(Collectors.joining(System.lineSeparator())))
                .build();
    }
}

