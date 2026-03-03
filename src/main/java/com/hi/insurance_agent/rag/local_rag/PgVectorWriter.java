package com.hi.insurance_agent.rag.local_rag;


import com.hi.insurance_agent.constant.DatasetConstant;
import com.hi.insurance_agent.db.SHASearch;
import com.hi.insurance_agent.util.CollectPaths;
import com.hi.insurance_agent.util.GetSha;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Component
public class PgVectorWriter {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private JdbcTemplate jdbcTemplate;


    public boolean pgVectorWrite(List<Document> splitDocuments) {
        List<String> allShaInDb = SHASearch.getAllSha(jdbcTemplate);
        List<Document> removeRepeatedSplitDocuments = new ArrayList<>();
        Set<String> shaSet = new HashSet<>(allShaInDb);

        // 给每个document算个文本的sha，用于去重复
        for (Document document : splitDocuments) {
            String textSha = GetSha.sha256(document.getText());
            if (!shaSet.contains(textSha)) {
                document.getMetadata().replace("sha", textSha);
                removeRepeatedSplitDocuments.add(document);
            } else
                log.info("Document {} has been in DB, abort! The text Sha256 is {}", document.getId(), textSha);
        }

        try {
            pgVectorVectorStore.add(removeRepeatedSplitDocuments);
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean doAllMdWrite() {
        List<String> dataPaths = CollectPaths.getAllDataPath(DatasetConstant.MARKDOWN_PATH, "md");
        MarkdownToDocumentsConverter converter = new MarkdownToDocumentsConverter();
        for (String dataPath : dataPaths) {
            List<Document> documents = converter.markdownToDocument(dataPath);
            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> splitDocuments = splitter.splitDocumentsCustomized(documents);
            try {
                pgVectorWrite(splitDocuments);
            } catch (Exception e) {
                log.error(e.getMessage());
                return false;
            }
        }
        return true;
    }
}
