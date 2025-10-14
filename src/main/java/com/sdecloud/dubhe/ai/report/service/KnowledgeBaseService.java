package com.sdecloud.dubhe.ai.report.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识库服务，负责加载和向量化知识库文档
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Service
public class KnowledgeBaseService {

	private final VectorStore vectorStore;

	@Value("classpath:知识库文档.md")
	private Resource knowledgeBaseFile;

	public KnowledgeBaseService(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	/**
	 * 应用启动时初始化知识库
	 * 读取知识库文档，进行文本分割，然后向量化存储
	 */
	@PostConstruct
	public void initKnowledgeBase() {
		try {
			log.info("开始加载知识库文档: {}", knowledgeBaseFile.getFilename());

			// 1. 读取文档
			TextReader textReader = new TextReader(knowledgeBaseFile);
			List<Document> documents = textReader.get();
			log.info("文档读取完成，共 {} 个文档", documents.size());

			// 2. 文本分割（每个chunk大小为800个token，重叠100个token）
			TokenTextSplitter textSplitter = new TokenTextSplitter(800, 100, 5, 10000, false);
			List<Document> splitDocuments = textSplitter.apply(documents);
			log.info("文档分割完成，共 {} 个文档片段", splitDocuments.size());

			// 3. 向量化并存储到向量库
			vectorStore.add(splitDocuments);
			log.info("知识库向量化完成，已存储 {} 个向量", splitDocuments.size());

		} catch (Exception e) {
			log.error("知识库初始化失败", e);
			throw new RuntimeException("知识库初始化失败", e);
		}
	}

	/**
	 * 根据问题查询相似的知识库内容
	 *
	 * @param question 用户问题
	 * @param topK     返回最相似的K个结果
	 * @return 相似的文档列表
	 */
	public List<Document> searchSimilarDocuments(String question, int topK) {
		log.info("查询相似问题: {}, topK: {}", question, topK);
		SearchRequest searchRequest = SearchRequest.builder()
				.query(question)
				.topK(topK)
				.build();
		List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);
		log.info("找到 {} 个相似文档", similarDocs.size());
		return similarDocs;
	}

}

