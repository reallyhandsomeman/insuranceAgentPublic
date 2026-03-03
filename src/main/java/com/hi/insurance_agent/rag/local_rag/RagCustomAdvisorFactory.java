package com.hi.insurance_agent.rag.local_rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.jdbc.core.JdbcTemplate;

public class RagCustomAdvisorFactory {

    /**
     * 混合检索的RAG拦截器
     *
     * @return
     */
    public static Advisor createHybridRagAdvisor(JdbcTemplate jdbc, EmbeddingModel embeddingModel) {
        InsuranceKnowledgeRetriever insuranceKnowledgeRetriever = new InsuranceKnowledgeRetriever(jdbc, embeddingModel);
        insuranceKnowledgeRetriever.setTopK(30); // 语义从数据库挑选出30个
        ReRankModel reRankModel = new ReRankModel();
        reRankModel.setTopK(5); // 精排5个
        QueryAugmenter queryAugmenter = QueryAugmentedFactory.createInstance();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(insuranceKnowledgeRetriever)
                .queryAugmenter(queryAugmenter)
                .documentPostProcessors(reRankModel)
                .build();
    }
}
