package com.hi.insurance_agent.rag.local_rag;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Setter
public class ReRankModel implements DocumentPostProcessor {
    int topK;

    @NotNull
    @Override
    public List<Document> process(@NotNull Query query, List<Document> documents) {
        LocalDate now = LocalDate.now();
        List<Document> newData = documents.stream()
                .sorted(Comparator.comparingDouble((Document d) -> {
                    Map<String, Object> metadata = d.getMetadata();

                    // 1. 计算时间得分：effective_date 距今天越近，得分越高（0~1）
                    LocalDate effectiveDate = null;
                    try {
                        if (metadata.containsKey("effective_date")) {
                            effectiveDate = LocalDate.parse((String) metadata.get("effective_date"));
                        }
                    } catch (Exception ignored) {
                    }
                    double timeScore = 0.0;
                    if (effectiveDate != null) {
                        double yearsDiff = Math.min(5.0, ChronoUnit.DAYS.between(effectiveDate, now) / 365.0);
                        timeScore = Math.max(0, 1 - yearsDiff / 5.0);
                    }

                    // 2. ESG 加分项
                    boolean esg = Boolean.parseBoolean(String.valueOf(metadata.getOrDefault("esg", "false")));
                    double esgScore = esg ? 1.0 : 0.0;

                    // 3. 废止声明分项
                    boolean abolished = Boolean.parseBoolean(String.valueOf(metadata.getOrDefault("abolition_statement", "false")));
                    double abolishPenalty = abolished ? 0.0 : 1.0;

                    // 4. 计算综合分 hybrid_score
                    double hybridScore = 0.25 * timeScore + 0.25 * esgScore + 0.5 * abolishPenalty;

                    return -(0.25 * hybridScore + 0.75 * d.getScore()); // 排序时得分高的排前面（因为Comparator默认升序）
                }))
                .toList()
                .subList(0, Math.min(documents.size(), topK));
        return newData;
    }
}