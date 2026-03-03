package com.hi.insurance_agent.rag.ali_cloud_rag;

public class ClientBuilder {

    /**
     * 初始化客户端（Client）。
     *
     * @return 配置好的客户端对象
     */
    public static com.aliyun.bailian20231229.Client createClient(String AccessKeyId, String AccessKeySecret) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(AccessKeyId)
                .setAccessKeySecret(AccessKeySecret);
        // 下方接入地址以公有云的公网接入地址为例，可按需更换接入地址。
        config.endpoint = "bailian.cn-beijing.aliyuncs.com";
        return new com.aliyun.bailian20231229.Client(config);
    }
}
