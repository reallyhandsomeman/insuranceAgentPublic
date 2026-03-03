package com.hi.insurance_agent.rag.ali_cloud_rag;

import com.aliyun.bailian20231229.models.RetrieveRequest;
import com.aliyun.bailian20231229.models.RetrieveResponse;
import com.aliyun.teautil.models.RuntimeOptions;

import java.util.List;
import java.util.Map;

public class KnowledgeBaseRetrieve {
    /**
     * 在指定的知识库中检索信息。
     *
     * @param client      客户端对象（bailian20231229Client）
     * @param workspaceId 业务空间ID
     * @param indexId     知识库ID
     * @param query       检索查询语句
     * @return 阿里云百炼服务的响应
     */
    public static RetrieveResponse retrieveIndex(com.aliyun.bailian20231229.Client client, String workspaceId, String indexId, String query) throws Exception {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.setIndexId(indexId);
        retrieveRequest.setQuery(query);
        RuntimeOptions runtime = new RuntimeOptions();
        return client.retrieveWithOptions(workspaceId, retrieveRequest, null, runtime);
    }

    /**
     * 在指定的知识库中检索信息，并且指定一个过滤器，过滤器格式详见：
     * <a href="https://help.aliyun.com/zh/model-studio/how-to-use-search-filters?spm=a2c4g.11186623.0.0.20b5ab47kpT3z2#38f1a6c52bk7v">...</a>。
     *
     * @param client      客户端对象（bailian20231229Client）
     * @param workspaceId 业务空间ID
     * @param indexId     知识库ID
     * @param query       检索查询语句
     * @return 阿里云百炼服务的响应
     */
    public static RetrieveResponse retrieveIndexWithFilter(com.aliyun.bailian20231229.Client client, String workspaceId, String indexId, List<Map<String, String>> searchFilters, String query) throws Exception {
        RetrieveRequest retrieveRequest = new RetrieveRequest();
        retrieveRequest.setIndexId(indexId);
        retrieveRequest.setQuery(query);
        retrieveRequest.setSearchFilters(searchFilters);
        RuntimeOptions runtime = new RuntimeOptions();
        return client.retrieveWithOptions(workspaceId, retrieveRequest, null, runtime);
    }

}