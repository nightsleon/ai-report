package com.sdecloud.dubhe.ai.report.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 聊天控制器，用于与 DashScope 大模型交互
 *
 * @author liangjun
 * @since 2025-10-14
 */
@RestController
@RequestMapping("/ai")
public class ChatController {

	private final ChatClient chatClient;

	public ChatController(ChatModel chatModel) {
		this.chatClient = ChatClient.builder(chatModel).build();
	}

	/**
	 * 简单聊天接口
	 *
	 * @param message 用户消息
	 * @return AI 响应
	 */
	@GetMapping("/chat")
	public String chat(@RequestParam(value = "message", defaultValue = "你好") String message) {
		return chatClient.prompt()
				.user(message)
				.call()
				.content();
	}

}

