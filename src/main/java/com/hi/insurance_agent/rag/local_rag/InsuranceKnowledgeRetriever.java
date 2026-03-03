package com.hi.insurance_agent.rag.local_rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 知识库混合检索器，继承DocumentRetriever
 */
@Slf4j
public class InsuranceKnowledgeRetriever implements DocumentRetriever {

    JdbcTemplate jdbc;

    EmbeddingModel embeddingModel;

    int topK = 30;

    public InsuranceKnowledgeRetriever setTopK(int topK) {
        this.topK = topK;
        return this;
    }

    public InsuranceKnowledgeRetriever(JdbcTemplate jdbc, EmbeddingModel embeddingModel) {
        this.jdbc = jdbc;
        this.embeddingModel = embeddingModel;
    }

    /**
     * 混合检索，综合考虑向量，倒排索引和日期排序以及ESG加分
     *
     * @param query 查询
     * @return List<Document>
     */
    @NotNull
    @Override
    public List<Document> retrieve(Query query) {
        String text = query.text();
        float[] queryEmbedding = embeddingModel.embed(text);
        String vectorString = Arrays.toString(queryEmbedding).replaceAll("\\s+", "");

        String sql = """
                SELECT 
                    id::text,
                    content,
                    metadata,
                    0.7 * (1 - (embedding <=> ?::vector)) 
                      + 0.3 * ts_rank(tsv, plainto_tsquery('chinese', ?)) AS hybrid_score
                FROM insurance_vector_store
                ORDER BY hybrid_score DESC
                LIMIT ?;
                """;

        List<Map<String, Object>> rows = jdbc.queryForList(
                sql,
                vectorString,                     // embedding 向量
                text,                            // ts_rank 使用的关键词
                topK                             // 返回数量
        );
        ObjectMapper mapper = new ObjectMapper();
        List<Document> data = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            Object meta = row.get("metadata");
            Map<String, Object> metaMap = null;
            if (meta != null) {
                try {
                    // 如果是 JSON 字符串，直接解析为 Map
                    metaMap = mapper.readValue(meta.toString(), new TypeReference<>() {
                    });
                } catch (Exception e) {
                    // 出错时原样放回
                    log.info(e.getMessage());
                }
            }
            if (metaMap != null) {
                Document doc = Document.builder()
                        .metadata(metaMap)
                        .id((String) row.get("id"))
                        .text((String) row.get("content"))
                        .score((Double) row.get("hybrid_score"))
                        .build();
                data.add(doc);
            }
        }
        return data;
    }
}