package com.sdecloud.dubhe.ai.report.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NL2SQL 服务
 * 负责将自然语言问题转换为 SQL 查询语句
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Service
public class Nl2SqlService {

	private final ChatClient chatClient;
	private final KnowledgeBaseService knowledgeBaseService;
	private final SystemPromptTemplate systemPromptTemplate;
	private final PromptTemplate userPromptTemplate;

	public Nl2SqlService(ChatModel chatModel, KnowledgeBaseService knowledgeBaseService,
	                     @Value("classpath:prompts/nl2sql-system-prompt.txt") Resource systemPromptResource,
	                     @Value("classpath:prompts/nl2sql-user-prompt.txt") Resource userPromptResource) {
		this.chatClient = ChatClient.builder(chatModel).build();
		this.knowledgeBaseService = knowledgeBaseService;
		
		// 初始化 Prompt 模板（只创建一次）
		this.systemPromptTemplate = new SystemPromptTemplate(systemPromptResource);
		this.userPromptTemplate = new PromptTemplate(userPromptResource);
	}

	/**
	 * 将自然语言问题转换为 SQL 查询语句
	 *
	 * @param question 用户的自然语言问题
	 * @param topK     检索相似文档数量
	 * @return 生成的 SQL 查询语句
	 */
	public String convertToSql(String question, int topK) {
		log.info("开始 NL2SQL 转换，问题: {}, topK: {}", question, topK);

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

		log.info("SQL 生成成功，长度: {} 字符", sql.length());
		return sql;
	}

}

