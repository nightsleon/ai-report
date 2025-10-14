package com.sdecloud.dubhe.ai.report.controller;

import com.sdecloud.dubhe.ai.report.service.KnowledgeBaseService;
import lombok.Data;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库查询控制器
 *
 * @author liangjun
 * @since 2025-10-14
 */
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

	private final KnowledgeBaseService knowledgeBaseService;

	public KnowledgeController(KnowledgeBaseService knowledgeBaseService) {
		this.knowledgeBaseService = knowledgeBaseService;
	}

	/**
	 * 查询相似问题接口
	 *
	 * @param question 用户问题
	 * @param topK     返回最相似的K个结果，默认3个
	 * @return 相似文档结果
	 */
	@GetMapping("/search")
	public SearchResponse searchSimilar(
			@RequestParam("question") String question,
			@RequestParam(value = "topK", defaultValue = "3") int topK) {

		List<Document> documents = knowledgeBaseService.searchSimilarDocuments(question, topK);

		List<DocumentResult> results = documents.stream()
				.map(doc -> {
					DocumentResult result = new DocumentResult();
					result.setContent(doc.getText());
					result.setMetadata(doc.getMetadata());
					return result;
				})
				.collect(Collectors.toList());

		SearchResponse response = new SearchResponse();
		response.setQuestion(question);
		response.setTopK(topK);
		response.setTotal(results.size());
		response.setResults(results);

		return response;
	}

	/**
	 * 查询结果响应类
	 */
	@Data
	public static class SearchResponse {
		private String question;
		private int topK;
		private int total;
		private List<DocumentResult> results;
	}

	/**
	 * 文档结果类
	 */
	@Data
	public static class DocumentResult {
		private String content;
		private Object metadata;
	}

}

