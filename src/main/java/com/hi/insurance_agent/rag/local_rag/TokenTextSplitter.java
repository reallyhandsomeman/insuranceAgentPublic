package com.hi.insurance_agent.rag.local_rag;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TokenTextSplitter {
    public List<Document> splitDocumentsDefault(List<Document> documents) {
        org.springframework.ai.transformer.splitter.TokenTextSplitter splitter = new org.springframework.ai.transformer.splitter.TokenTextSplitter();
        return splitter.apply(documents);
    }

    /**
     * 自定义将文本输入切成chunks，chunkSize为1000左右比较合适
     *
     * @param documents 文本
     * @return 切片后的结果
     */
    public List<Document> splitDocumentsCustomized(List<Document> documents) {
        org.springframework.ai.transformer.splitter.TokenTextSplitter splitter = new org.springframework.ai.
                transformer.splitter.TokenTextSplitter
                (1000, 350, 5, 10000, true);
        return splitter.apply(documents);
    }
}
