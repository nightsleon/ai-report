package com.sdecloud.dubhe.ai.report.controller;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.sdecloud.dubhe.ai.report.graph.Nl2SqlNode;
import com.sdecloud.dubhe.ai.report.service.KnowledgeBaseService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG (检索增强生成) 控制器
 * 结合知识库检索和大模型对话，提供更准确的答案
 *
 * @author liangjun
 * @since 2025-10-14
 */
@RestController
@RequestMapping("/rag")
public class RagController {

	private final ChatClient chatClient;
	private final KnowledgeBaseService knowledgeBaseService;
	private final Nl2SqlNode nl2SqlNode;

	public RagController(ChatModel chatModel, 
	                     KnowledgeBaseService knowledgeBaseService,
	                     Nl2SqlNode nl2SqlNode) {
		this.chatClient = ChatClient.builder(chatModel).build();
		this.knowledgeBaseService = knowledgeBaseService;
		this.nl2SqlNode = nl2SqlNode;
	}

	/**
	 * RAG 问答接口
	 * 1. 先从知识库中检索相关内容
	 * 2. 将检索结果作为上下文，结合用户问题调用大模型
	 *
	 * @param question 用户问题
	 * @param topK     检索相似文档数量
	 * @return AI 基于知识库的回答
	 */
	@GetMapping("/chat")
	public String chat(
			@RequestParam("question") String question,
			@RequestParam(value = "topK", defaultValue = "3") int topK) {

		// 1. 从知识库检索相关内容
		List<Document> similarDocs = knowledgeBaseService.searchSimilarDocuments(question, topK);

		// 2. 构建上下文
		String context = similarDocs.stream()
				.map(Document::getText)
				.collect(Collectors.joining("\n\n"));

		// 3. 构建提示词
		String prompt = String.format("""
				你是一个专业的数据分析助手。请根据以下知识库内容回答用户的问题。
				
				知识库内容：
				%s
				
				用户问题：%s
				
				请基于知识库内容给出准确、详细的回答。如果知识库中没有相关信息，请明确告知用户。
				""", context, question);

		// 4. 调用大模型
		return chatClient.prompt()
				.user(prompt)
				.call()
				.content();
	}

	/**
	 * NL2SQL 接口
	 * 将自然语言问题转换为可执行的 SQL 查询语句
	 *
	 * @param question 用户的自然语言问题
	 * @param topK     检索相似文档数量，默认3
	 * @return 生成的 SQL 查询语句
	 */
	@GetMapping("/nl2sql")
	public String nl2sql(
			@RequestParam("question") String question,
			@RequestParam(value = "topK", defaultValue = "3") int topK) {
		
		// 创建状态并调用Nl2SqlNode
		OverAllState state = new OverAllState();
		Map<String, Object> input = new HashMap<>(4);
		input.put("question", question);
		input.put("topK", topK);
		state.input(input);

		// 调用节点
		Map<String, Object> result = nl2SqlNode.apply(state);
		
		// 返回SQL
		return (String) result.get("sql");
	}

}

