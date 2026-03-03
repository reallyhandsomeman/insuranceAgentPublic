package com.hi.insurance_agent.rag.local_rag;

import com.hi.insurance_agent.util.MarkdownMetadataExtract;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 负责读取所有 Markdown 文档并转换为 Document 列表，存储到向量数据库
 */
@Component
public class MarkdownToDocumentsConverter {

    public List<Document> markdownToDocument(String markdownFilePath) {
        Map<String, String> metadata = MarkdownMetadataExtract.getMetadata(markdownFilePath);
        String title = metadata.get("title");
        String category = metadata.get("category");
        String abolition_statement = metadata.get("abolition_statement");
        String index = metadata.get("index");
        String effective_date = metadata.get("effective_date");
        String path = metadata.get("path");
        String esg = metadata.get("esg");
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .withAdditionalMetadata("title", title)
                .withAdditionalMetadata("index", index)
                .withAdditionalMetadata("effective_date", effective_date)
                .withAdditionalMetadata("category", category)
                .withAdditionalMetadata("abolition_statement", abolition_statement)
                .withAdditionalMetadata("esg", esg)
                .withAdditionalMetadata("path", path)
                .withAdditionalMetadata("sha", "")
                .build();
        // 转换为系统资源类，输入给MarkdownDocumentReader
        FileSystemResource resource = new FileSystemResource(markdownFilePath);
        MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
        return reader.get();
    }
}
