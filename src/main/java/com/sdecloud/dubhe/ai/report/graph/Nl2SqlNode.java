package com.sdecloud.dubhe.ai.report.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.sdecloud.dubhe.ai.report.service.KnowledgeBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自然语言转换SQL节点
 * 负责将用户问题转换为SQL查询语句
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Component
public class Nl2SqlNode implements NodeAction {

    private final ChatClient chatClient;
    private final KnowledgeBaseService knowledgeBaseService;
    private final SystemPromptTemplate systemPromptTemplate;
    private final PromptTemplate userPromptTemplate;

    public Nl2SqlNode(ChatModel chatModel, KnowledgeBaseService knowledgeBaseService,
                      @Value("classpath:prompts/nl2sql-system-prompt.txt") Resource systemPromptResource,
                      @Value("classpath:prompts/nl2sql-user-prompt.txt") Resource userPromptResource) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.knowledgeBaseService = knowledgeBaseService;
        
        // 初始化 Prompt 模板（只创建一次）
        this.systemPromptTemplate = new SystemPromptTemplate(systemPromptResource);
        this.userPromptTemplate = new PromptTemplate(userPromptResource);
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        log.info("执行自然语言转换SQL节点");

        String question = state.value("question", "");
        Integer topK = state.value("topK", 5);

        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("问题不能为空");
        }

        log.debug("转换问题: {}, topK: {}", question, topK);

        try {
            // 1. 从知识库检索相关内容
            List<Document> similarDocs = knowledgeBaseService.searchSimilarDocuments(question, topK);
            log.debug("检索到 {} 个相似文档", similarDocs.size());

            // 2. 构建知识库上下文
            String context = similarDocs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n"));

            // 3. 创建系统 Prompt 模板（系统角色提示）
            Message systemMessage = systemPromptTemplate.createMessage();

            // 4. 创建用户 Prompt 模板（用户问题 + 知识库上下文）
            Message userMessage = userPromptTemplate.createMessage(
                    Map.of("context", context, "question", question)
            );

            // 5. 调用大模型生成 SQL
            log.debug("调用大模型生成 SQL...");
            String sql = chatClient.prompt()
                    .messages(systemMessage, userMessage)
                    .call()
                    .content();

            log.info("SQL转换成功，长度: {} 字符", sql.length());
            log.debug("生成的SQL: {}", sql);

            return Map.of(
                    "sql", sql,
                    "nl2sql_success", true
            );

        } catch (Exception e) {
            log.error("SQL转换失败", e);
            return Map.of(
                    "nl2sql_success", false,
                    "nl2sql_error", e.getMessage()
            );
        }
    }
}
