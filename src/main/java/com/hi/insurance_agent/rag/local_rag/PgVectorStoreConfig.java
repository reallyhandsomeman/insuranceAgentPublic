package com.hi.insurance_agent.rag.local_rag;


import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class PgVectorStoreConfig {
    @Value("${spring.ai.vectorstore.pgvector.dimensions}")
    int dimension;


    /**
     * @param jdbcTemplate
     * @param dashscopeEmbeddingModel
     * @return
     */
//    @DependsOn("jschSession") // 必须先完成jschSession，即ssh通道构建
    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .batchingStrategy(new DashscopeEmbeddingApiBatchingStrategy())
                .dimensions(dimension)                     // Optional: defaults to model dimensions or 384
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("insurance_vector_store")     // Optional: defaults to "vector_store"
                // ⬇️maxDocumentBatchSize 决定「每次批量写入 PgVector 表时处理的文档数量上限」
                // 每次最多批量写100条
                .maxDocumentBatchSize(100)
                .build();
    }
}
