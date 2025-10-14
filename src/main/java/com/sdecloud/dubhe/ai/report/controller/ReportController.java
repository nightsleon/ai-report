package com.sdecloud.dubhe.ai.report.controller;

import com.sdecloud.dubhe.ai.report.model.ReportRequest;
import com.sdecloud.dubhe.ai.report.model.ReportResponse;
import com.sdecloud.dubhe.ai.report.model.QueryResponse;
import com.sdecloud.dubhe.ai.report.service.ReportGenerationService;
import com.sdecloud.dubhe.ai.report.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 数据分析报告控制器
 * 提供从问题到报告的完整流程
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@RestController
@RequestMapping("/report")
public class ReportController {

	private final ReportGenerationService reportGenerationService;
	@SuppressWarnings("unused") // 用于初始化报告目录
	private final FileUtils fileUtils;
	
	public ReportController(ReportGenerationService reportGenerationService, FileUtils fileUtils) {
		this.reportGenerationService = reportGenerationService;
		this.fileUtils = fileUtils;
		// 确保报告目录存在
		fileUtils.ensureReportDirectory();
	}

	/**
	 * 仅执行 NL2SQL + 查询（不生成报告）
	 *
	 * @param question 用户问题
	 * @param topK     知识库检索数量
	 * @return 查询结果
	 */
	@GetMapping("/query")
	public QueryResponse query(
			@RequestParam("question") String question,
			@RequestParam(value = "topK", defaultValue = "3") int topK) {
		return reportGenerationService.query(question, topK);
	}

	/**
	 * 生成完整的数据分析报告
	 * 流程：NL2SQL → 执行查询 → 生成图表 → 生成报告
	 *
	 * @param request 报告生成请求
	 * @return 完整的分析报告（Markdown 格式）
	 */
	@PostMapping("/generate")
	public ReportResponse generateReport(@RequestBody ReportRequest request) {
		return reportGenerationService.generateReport(request);
	}

}

