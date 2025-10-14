package com.sdecloud.dubhe.ai.report.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量存储配置类
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Configuration
public class VectorStoreConfig {

	/**
	 * 创建内存向量存储 Bean
	 * SimpleVectorStore 是一个简单的内存向量存储实现
	 *
	 * @param embeddingModel 向量模型
	 * @return VectorStore 实例
	 */
	@Bean
	public VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return SimpleVectorStore.builder(embeddingModel).build();
	}

}

