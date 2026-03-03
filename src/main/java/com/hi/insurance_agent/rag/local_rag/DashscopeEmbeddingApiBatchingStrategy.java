package com.hi.insurance_agent.rag.local_rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.ContentFormatter;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;

import java.util.ArrayList;
import java.util.List;

/**
 * 符合api调用embedding的上传限制的 数据batch策略
 */
@Slf4j
public class DashscopeEmbeddingApiBatchingStrategy implements BatchingStrategy {

    final int MAX_DOCS_PER_BATCH = 10;      // 模型限制
    final int MAX_TOKENS_PER_DOC = 8192;    // 模型限制
    private final TokenCountEstimator tokenCountEstimator;
    private final ContentFormatter contentFormatter;
    private final MetadataMode metadataMode;

    public DashscopeEmbeddingApiBatchingStrategy() {
        this.contentFormatter = Document.DEFAULT_CONTENT_FORMATTER;
        this.metadataMode = MetadataMode.NONE;
        this.tokenCountEstimator = new JTokkitTokenCountEstimator();
    }

    /**
     * 根据 DashScope text-embedding-v4 限制，将文档列表分批：
     * - 每个文档 ≤ 8192 tokens（超限自动切分）
     * - 每批 ≤ 10 文档
     */
    @Override
    public List<List<Document>> batch(List<Document> documents) {
        List<List<Document>> batches = new ArrayList<>();
        List<Document> currentBatch = new ArrayList<>();

        List<Document> processedDocs = new ArrayList<>();

        // ① 预处理文档：切分超限文档
        for (Document doc : documents) {
            int tokenCount = tokenCountEstimator.estimate(
                    doc.getFormattedContent(contentFormatter, metadataMode));

            if (tokenCount > MAX_TOKENS_PER_DOC) {
                TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
                List<Document> subDocs = tokenTextSplitter.splitDocumentsDefault(List.of(doc));
                log.warn("文档超出单条上限（{} tokens），已拆分为 {} 个子文档：{}",
                        tokenCount, subDocs.size(), doc.getMetadata());
                processedDocs.addAll(subDocs);
            } else {
                processedDocs.add(doc);
            }
        }

        // ② 按条数分批
        for (Document doc : processedDocs) {
            currentBatch.add(doc);
            if (currentBatch.size() >= MAX_DOCS_PER_BATCH) {
                batches.add(new ArrayList<>(currentBatch));
                currentBatch.clear();
            }
        }

        // ③ 收尾
        if (!currentBatch.isEmpty()) {
            batches.add(currentBatch);
        }

        log.info("共生成 {} 个批次，总文档数 {}", batches.size(), processedDocs.size());
        return batches;
    }
}