package com.hi.insurance_agent.rag.ali_cloud_rag;

import com.aliyun.bailian20231229.models.AddFileResponse;
import com.aliyun.bailian20231229.models.ApplyFileUploadLeaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static com.hi.insurance_agent.rag.ali_cloud_rag.KnowledgeBaseCreate.*;

public class InsuranceFileUploader {

    private final com.aliyun.bailian20231229.Client client;

    public InsuranceFileUploader(com.aliyun.bailian20231229.Client client) {
        this.client = client;
    }

    public String uploadOneFile(String filePath, String fileName, String workspaceId, List<String> tags) {
        // 设置默认值
        String categoryId = "default";
        String parser = "DASHSCOPE_DOCMIND";
        try {
            // 步骤1：初始化客户端（Client）
            System.out.println("步骤1：初始化Client成功");

            // 步骤2：准备文件信息
            System.out.println("步骤2：准备文件信息");
            String fileMd5 = calculateMD5(filePath);
            String fileSize = getFileSize(filePath);

            // 步骤3：申请上传租约
            System.out.println("步骤3：向阿里云百炼申请上传租约");
            ApplyFileUploadLeaseResponse leaseResponse = applyLease(client, categoryId, fileName, fileMd5, fileSize, workspaceId);
            String leaseId = leaseResponse.getBody().getData().getFileUploadLeaseId();
            String uploadUrl = leaseResponse.getBody().getData().getParam().getUrl();
            Object uploadHeaders = leaseResponse.getBody().getData().getParam().getHeaders();

            // 步骤4：上传文件
            System.out.println("步骤4：上传文件到阿里云百炼");
            // 请自行安装jackson-databind
            // 将上一步的uploadHeaders转换为Map(Key-Value形式)
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> uploadHeadersMap = (Map<String, String>) mapper.readValue(mapper.writeValueAsString(uploadHeaders), Map.class);
            uploadFile(uploadUrl, uploadHeadersMap, filePath);

            // 步骤5：将文件添加到服务器
            System.out.println("步骤5：将文件添加到阿里云百炼服务器");
            AddFileResponse addResponse = addFile(client, leaseId, parser, categoryId, workspaceId, tags);
            String fileId = addResponse.getBody().getData().getFileId();
            System.out.println("文件ID为：" + fileId);
            return fileId;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}




