# AI Report - 智能数据分析报告系统

基于 Spring Boot + Spring AI Alibaba 的智能数据分析报告系统，支持自然语言问答、NL2SQL、数据查询、图表生成和分析报告生成。

## ✨ 功能特性

- 🤖 **智能对话**: 基于 DashScope 大模型的智能问答
- 📚 **知识库检索**: 向量化知识库，支持语义检索
- 🔍 **RAG 增强**: 基于知识库的检索增强生成
- 🔄 **NL2SQL**: 自然语言转 SQL 查询
- 📊 **数据查询**: 通过 MCP 工具执行 SQL 查询
- 📈 **图表生成**: 通过 MCP 工具生成可视化图表
- 📝 **分析报告**: 生成 Markdown 格式的专业数据分析报告
- 💾 **报告保存**: 自动将报告保存到 `report-result/` 目录
- 📄 **Word 转换**: 使用 Pandoc 将 Markdown 转换为 Word 文档
- 🏗️ **架构优化**: 清晰的分层架构，易于维护和扩展

## 🛠️ 技术栈

- **Spring Boot**: 3.4.0
- **Spring AI Alibaba**: 1.0.0.3
- **DashScope**: 阿里云百炼大模型平台
- **JDK**: 17
- **Maven**: 3.9+
- **Pandoc**: Markdown 转 Word 文档
- **Lombok**: 简化 Java 代码

## 📁 项目结构

```
ai-report/
├── src/main/java/com/sdecloud/dubhe/ai/report/
│   ├── controller/              # 控制器层
│   │   ├── ChatController.java
│   │   ├── KnowledgeController.java
│   │   ├── RagController.java
│   │   └── ReportController.java
│   ├── service/                 # 服务层
│   │   ├── DataAnalysisService.java
│   │   ├── KnowledgeBaseService.java
│   │   ├── Nl2SqlService.java
│   │   └── ReportGenerationService.java
│   ├── util/                    # 工具层
│   │   ├── FileUtils.java
│   │   └── PandocUtils.java
│   ├── model/                   # 模型层
│   │   ├── ReportRequest.java
│   │   ├── ReportResponse.java
│   │   └── QueryResponse.java
│   └── config/                  # 配置层
│       └── VectorStoreConfig.java
├── src/main/resources/
│   ├── prompts/                 # Prompt 模板
│   │   ├── nl2sql-system-prompt.txt
│   │   ├── nl2sql-user-prompt.txt
│   │   ├── report-analyst-system-prompt.txt
│   │   └── report-analyst-user-prompt.txt
│   ├── application.yml          # 应用配置
│   └── 知识库文档.md             # 知识库文档
├── report-result/               # 报告保存目录
├── report-test.http             # HTTP 测试用例
├── test-pandoc.sh              # Pandoc 测试脚本
└── run.sh                      # 启动脚本
```

## 🚀 快速开始

### 1. 环境要求

- **JDK 17+**
- **Maven 3.9+**
- **Pandoc** (用于 Markdown 转 Word)

### 2. 安装 Pandoc

```bash
# macOS
brew install pandoc

# Ubuntu/Debian
sudo apt-get install pandoc

# Windows
# 下载并安装: https://pandoc.org/installing.html
```

### 3. 配置 API Key

#### 方式 1：环境变量（推荐）

```bash
export DASHSCOPE_API_KEY=sk-your-actual-api-key
```

#### 方式 2：修改配置文件

编辑 `src/main/resources/application.yml`：
```yaml
spring:
  ai:
    dashscope:
      api-key: sk-your-actual-api-key
```

**获取 API Key**: 访问 [DashScope 控制台](https://dashscope.console.aliyun.com/)

### 4. 启动应用

```bash
# 使用启动脚本（推荐）
./run.sh

# 或使用 Maven 命令
mvn spring-boot:run
```

### 5. 验证启动

```bash
# 检查应用状态
curl http://localhost:8080/actuator/health

# 测试基础聊天
curl "http://localhost:8080/ai/chat?message=你好"
```

## 📖 API 文档

### 1. 基础聊天
```http
GET /ai/chat?message={用户消息}
```

### 2. 知识库检索
```http
GET /knowledge/search?question={问题}&topK={数量}
```

### 3. RAG 增强问答
```http
GET /rag/chat?question={问题}&topK={数量}
```

### 4. NL2SQL 转换
```http
GET /rag/nl2sql?question={问题}&topK={数量}
```

### 5. 数据查询
```http
GET /report/query?question={问题}&topK={数量}
```

### 6. 生成完整报告
```http
POST /report/generate
Content-Type: application/json

{
  "question": "查询各部门的销售额",
  "topK": 3,
  "generateChart": true,
  "chartType": "bar",
  "generateWord": true
}
```

## 🧪 测试用例

项目提供了完整的 HTTP 测试用例文件：`report-test.http`

### 使用 IntelliJ IDEA / VS Code

1. 打开 `report-test.http` 文件
2. 点击测试用例旁边的 ▶️ 运行按钮
3. 查看响应结果

### 使用 curl 命令

```bash
# 基础聊天
curl "http://localhost:8080/ai/chat?message=你好"

# 知识库检索
curl "http://localhost:8080/knowledge/search?question=如何分析销售数据&topK=3"

# 生成完整报告
curl -X POST http://localhost:8080/report/generate \
  -H "Content-Type: application/json" \
  -d '{
    "question": "查询各部门的销售额",
    "topK": 3,
    "generateChart": true,
    "chartType": "bar",
    "generateWord": true
  }'
```

## 📊 报告生成流程

### 完整流程
```
用户问题 → NL2SQL → 执行查询 → 生成图表 → 生成报告 → 保存文件 → 转换 Word
```

### 生成的文件
```
report-result/
├── 20251014_170030_查询各部门的销售额.md     # Markdown 报告
└── 20251014_170030_查询各部门的销售额.docx   # Word 文档
```

## 🔧 配置说明

### MCP 工具配置

如果使用 MCP 工具进行数据查询和图表生成，需要配置：

```yaml
spring:
  ai:
    mcp:
      client:
        toolcallback:
          enabled: true
        sse:
          connections:
            mysqlDataQuery:
              url: http://10.133.0.21:18088/
            antvChart:
              url: http://10.133.0.21:18089/
        request-timeout: 60000
```

### 日志配置

```yaml
logging:
  level:
    org.springframework.ai: INFO
    com.alibaba.cloud.ai: DEBUG
    com.sdecloud.dubhe.ai.report: DEBUG
```

## 🏗️ 架构设计

### 分层架构
```
Controller Layer (控制器层)
    ↓
Service Layer (服务层)
    ↓
Util Layer (工具层)
    ↓
Model Layer (模型层)
```

### 核心组件

#### 1. Controller 层
- **ReportController**: 报告生成接口
- **RagController**: RAG 问答接口
- **KnowledgeController**: 知识库检索接口
- **ChatController**: 基础聊天接口

#### 2. Service 层
- **ReportGenerationService**: 报告生成业务协调
- **DataAnalysisService**: 数据分析服务
- **Nl2SqlService**: NL2SQL 转换服务
- **KnowledgeBaseService**: 知识库服务

#### 3. Util 层
- **FileUtils**: 文件操作工具
- **PandocUtils**: 文档转换工具

#### 4. Model 层
- **ReportRequest/Response**: 报告相关模型
- **QueryResponse**: 查询响应模型

## 🚨 常见问题

### Q1: 启动时报错 "Invalid API-key provided"

**原因**: DashScope API Key 未配置或无效。

**解决方案**:
1. 访问 [DashScope 控制台](https://dashscope.console.aliyun.com/) 获取 API Key
2. 设置环境变量：`export DASHSCOPE_API_KEY=sk-xxxxxxxx`
3. 或直接修改 `application.yml` 配置文件

### Q2: 报告保存失败

**原因**: 没有写入权限或目录不存在。

**解决方案**:
```bash
mkdir -p report-result
chmod 755 report-result
```

### Q3: Pandoc 转换失败

**原因**: Pandoc 未安装或路径不正确。

**解决方案**:
```bash
# 安装 Pandoc
brew install pandoc

# 验证安装
pandoc --version
```

### Q4: MCP 工具调用失败

**原因**: MCP 服务未启动或地址配置错误。

**解决方案**: 检查 `application.yml` 中的 MCP 服务地址配置。

## 🔄 开发指南

### 添加新的 Prompt 模板

1. 在 `src/main/resources/prompts/` 目录下创建新的 `.txt` 文件
2. 在对应的 Service 类中注入资源
3. 创建 `PromptTemplate` 或 `SystemPromptTemplate` 实例

### 添加新的工具类

1. 在 `src/main/java/com/sdecloud/dubhe/ai/report/util/` 目录下创建工具类
2. 使用 `@Component` 注解标记为 Spring Bean
3. 在需要的地方注入使用

### 添加新的模型类

1. 在 `src/main/java/com/sdecloud/dubhe/ai/report/model/` 目录下创建模型类
2. 使用 `@Data` 注解简化代码
3. 定义清晰的字段和注释

## 📈 性能优化

### 已实现的优化

1. **Prompt 模板优化**: 将 `PromptTemplate` 提取为成员变量，避免重复创建
2. **分层架构**: 清晰的职责分离，提高代码可维护性
3. **依赖注入**: 使用构造函数注入，符合 Spring 最佳实践
4. **工具类复用**: 通用功能封装为工具类，提高代码复用性

### 建议的进一步优化

1. **缓存机制**: 对频繁查询的结果进行缓存
2. **异步处理**: 对耗时的报告生成使用异步处理
3. **连接池**: 对数据库连接使用连接池
4. **监控**: 添加应用性能监控

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👥 作者

- **liangjun** - *初始开发* - [GitHub](https://github.com/liangjun)

## 🙏 致谢

- [Spring AI](https://spring.io/projects/spring-ai) - Spring AI 框架
- [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba) - Spring AI Alibaba 集成
- [DashScope](https://dashscope.aliyun.com/) - 阿里云百炼大模型平台
- [Pandoc](https://pandoc.org/) - 文档转换工具

---

**最后更新**: 2025-10-14  
**版本**: 1.0.0  
**状态**: ✅ 生产就绪
