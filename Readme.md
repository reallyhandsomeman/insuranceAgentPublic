

#  Insurance-AI-Agent

本项目是一个以**保险业务为核心场景**的智能系统集合，包含：

- 基于 **Spring Boot + Spring AI + RAG + 工具调用（Tools API）** 的智能保险问答后端；
- 基于 **知识图谱的跨文档保险语料融合框架**  `knowledge_graph`(详见insurance-knowledge-graph-serve目录下的readme)

通过 **Spring Boot + Spring AI + 知识图谱 + RAG 检索增强 + 工具调用（Tools API）** 打造的智能保险助手。
支持：

*  自动解析保险 PDF → Markdown
*  构建本地向量知识库（pgvector）
*  智能检索相关保险条款
*  自主推理的保险咨询 Agent
*  工具调用（查询费率、读取文档等）
*  前后端分离架构
*  让寿险/健康险等保险产品在 **复杂文档 + 多源资料** 的情况下，也能做到 **完整、准确、可追溯** 的智能问答与检索

项目运行结果见技术报告

---

# 技术架构

问答机器人
```
用户提问
   ↓
Controller (AiController)
   ↓
InsuranceApp（应用层）
   ↓
RAG 检索（本地 pgvector 或 阿里云百炼知识库）
   ↓
工具调用（读文档 / 读费率 PDF / 获取保险列表）
   ↓
大模型生成最终答案
   ↓
返回前端 UI
```

智能体
```
用户提问
   ↓
Controller (AiController)
   ↓
InsuranceAgent
   ↓
返回前端 UI                 
```

## 智能体内部循环逻辑

在一次对话请求内部，智能体不是“一次推理就出答案”，而是可以多轮自我调用，流程类似：

1. 根据当前对话上下文 **思考 / 规划下一步动作**
2. 判断是否需要：
    - 调用 RAG 检索（本地 pgvector）
    - 调用工具（读文档 / 读费率 PDF / 获取保险列表等）
    - 或直接生成回复
3. 如果需要工具：
    - 调用对应 Tool（ReadMarkdownTool / ReadRatePdfTool / GetAllInsuranceTool 等）
    - 将工具返回结果写入上下文
    - 回到第 1 步，继续下一轮思考
4. 直到满足终止条件（如调用 TerminateTool 或达到最大轮数），再由大模型生成最终回答
---

# 📁 项目结构

```
├── Readme.md                     # 项目总体介绍、环境配置、快速启动指南
├── data                          # 数据集目录
│   ├── dirNumber-Insurance.csv   # 保险名称与编号映射
│   ├── documents                 # 原始 PDF 文档
│   ├── extract.py                # 抽取文件夹-保险名脚本
│   └── markdown                  # PDF 转换后的 Markdown 文档
├── insurance-ai-agent-frontend   # 前端项目（Vite + Vue/React）
├── insurance-ai-knowledge-graph-serve   # 知识图谱构建）
├── mvnw / mvnw.cmd               # Maven Wrapper
├── pom.xml                       # 项目依赖与构建
├── src
│   ├── main/java/com/hi/insurance_agent
│   │   ├── InsuranceAgentApplication.java  # Spring Boot 主启动类
│   │   ├── advisor/                        # AOP 日志切面
│   │   ├── agent/                          # 智能体核心逻辑
│   │   ├── app/                            # 应用层封装
│   │   ├── config/                         # 系统与跨域配置
│   │   ├── constant/                       # 常量 / Prompt
│   │   ├── controller/                     # AI 对话控制器
│   │   ├── data/                           # PDF/Markdown 转换工具
│   │   ├── db/                             # SHA 检索 / 数据层
│   │   ├── rag/                            # 本地 + 云 RAG 模块
│   │   ├── ssh2server/                     # SSH 隧道配置
│   │   ├── tools/                          # Tool API 工具实现
│   │   └── util/                           # 工具类
│   └── test                                # 单元测试
```

---

# ⚙️ 环境要求

| 工具                       | 版本      |
|--------------------------|---------|
| ☕ JDK                    | **21**  |
| 🛠 Spring AI             | **1.0.3** |
| 🐘 PostgreSQL（本地 RAG 可选） | 15      |
| 其他详细见pom.xml             |       |
---

# 📘 application.yml 配置示例（必填）

```yaml
spring:
  datasource:
    url: jdbc:postgresql://ip/db
    username: xxx
    password: xxx
    driver-class-name: org.postgresql.Driver
  application:
    name: insurance-ai-agent
  ai:
    vectorstore:
      pgvector:
        index-type: HNSW
        dimensions: 1024 # 嵌入向量的维度
        distance-type: COSINE_DISTANCE
        max-document-batch-size: 10000
    # ai-api
    dashscope: 
      api-key: xxx
      embedding:
        options:
          model: text-embedding-v4
          dimensions: 1024

      chat:
        options:
          model: qwen3-max

server:
  port: 8123
  servlet:
    context-path: /api
# springdoc-openapi
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  api-docs:
    path: /v3/api-docs
  group-configs:
    - group: 'default'
      paths-to-match: '/**'
      packages-to-scan: com.hi.insurance_agent.controller
# knife4j
knife4j:
  enable: true
  setting:
    language: zh_cn

logging:
  level:
    root: info

```

---

# 📚 RAG（检索增强）初始化指南

## ① 放置 PDF 文档数据集

```
/data/documents
```

## ② 运行 PDF → Markdown 转换

```
GenerateAllMarkdown.java
```

生成结果位于：

```
/data/markdown
```

## ③ 构建 pgvector 本地向量库（可选）

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

运行：

```
PgVectorWriter.doAllMdWrite()
```

---

#  后端启动方式

## 方法 1：IDEA 运行

运行：

```
InsuranceAgentApplication.java
```

## 方法 2：命令行运行

### Mac/Linux：

```
./mvnw spring-boot:run
```

### Windows：

```
mvnw.cmd spring-boot:run
```

---

# 测试方式

| 测试项   | 运行位置                             | 作用           |
| ----- | -------------------------------- | ------------ |
| 对话测试  | `InsuranceAppTest`               | 测试完整对话链路     |
| 智能体测试 | `InsuranceAgentApplicationTests` | 测试智能体推理、工具调用 |

---

# 前端启动方式

```
cd insurance-ai-agent-frontend
npm install
npm run dev
```

访问：

```
http://localhost:8123/
```


---

#  FAQ

### 1. No API Key Provided

→ 需要在 application.yml 写入 DashScope API Key。

### 2. pgvector 扩展不存在
参考技术报告中的安装方法
```sql
CREATE EXTENSION vector;
```

### 3. RAG 检索不到内容

检查：

* data/markdown 是否为空
* PgVectorWriter 是否执行
* PostgreSQL 配置是否正确

### 4. 前端跨域

后端已启用 `CorsConfig.java`。

---
