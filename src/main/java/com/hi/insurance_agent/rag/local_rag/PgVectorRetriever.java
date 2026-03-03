package com.hi.insurance_agent.rag.local_rag;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/*
 *   向量数据库的自定义检索器，目前无作用，仅测试使用
 */
@Component
public class PgVectorRetriever {


    @Resource
    VectorStore pgVectorStore;


    public List<Document> similaritySearch(SearchRequest searchRequest) {
        return pgVectorStore.similaritySearch(searchRequest);
    }

    /**
     * @param query
     * @param topK
     * @param similarityThreshold
     * @param filterExpression    元数据过滤表达式，例如："author in ['john', 'jill'] && article_type == 'blog'"
     * @return
     */
    public List<Document> similaritySearch(String query, int topK, double similarityThreshold, String filterExpression) {
        return pgVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(similarityThreshold)
                        .filterExpression(filterExpression).build());
    }


    @Resource
    JdbcTemplate jdbc;

    @Resource
    EmbeddingModel embeddingModel;

    /**
     * 混合检索，综合考虑向量，倒排索引和日期排序以及ESG加分
     *
     * @param query 查询
     * @param topK  返回数目
     * @return id, content, metadata
     */
    public List<Map<String, Object>> hybridSearch(String query, int topK) {

        float[] queryEmbedding = embeddingModel.embed(query);
        String vectorString = Arrays.toString(queryEmbedding).replaceAll("\\s+", "");

        String sql = """
                 SELECT
                   id,
                   content,
                   metadata,
                   0.6 * (1 - (embedding <=> ?::vector)) +
                   0.25 * ts_rank(tsv, plainto_tsquery('chinese', ?)) +
                   0.1 * GREATEST(
                         0,
                         1 - ((CURRENT_DATE - COALESCE((metadata->>'effective_date')::date, CURRENT_DATE))::float / (365 * 5))
                       ) +
                   0.05 * CASE
                           WHEN (metadata->>'esg')::boolean IS TRUE THEN 1
                           ELSE 0
                         END
                   - 0.2 * CASE
                           WHEN (metadata->>'abolition_statement')::boolean IS TRUE THEN 1
                           ELSE 0
                         END AS hybrid_score
                 FROM insurance_vector_store
                 WHERE tsv @@ plainto_tsquery('chinese', ?)
                 ORDER BY hybrid_score DESC
                 LIMIT ?;
                """;

        List<Map<String, Object>> rows = jdbc.queryForList(
                sql,
                vectorString,                     // embedding 向量
                query,                            // ts_rank 使用的关键词
                query,                            // WHERE 过滤的关键词
                topK                              // 返回数量
        );
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> data = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", row.get("id"));
            map.put("content", row.get("content"));

            Object meta = row.get("metadata");
            if (meta != null) {
                try {
                    // 如果是 JSON 字符串，直接解析为 Map
                    Map<String, Object> metaMap = mapper.readValue(meta.toString(), new TypeReference<>() {
                    });
                    map.put("metadata", metaMap);
                } catch (Exception e) {
                    // 出错时原样放回
                    map.put("metadata", meta);
                }
            }
            data.add(map);
        }
        return data;
    }
}
