package com.sdecloud.dubhe.ai.report.service;

import com.sdecloud.dubhe.ai.report.model.QueryResponse;
import com.sdecloud.dubhe.ai.report.model.ReportRequest;
import com.sdecloud.dubhe.ai.report.model.ReportResponse;
import com.sdecloud.dubhe.ai.report.util.FileUtils;
import com.sdecloud.dubhe.ai.report.util.PandocUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 报告生成服务
 * 负责协调各个服务完成报告生成流程
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final Nl2SqlService nl2SqlService;
    private final DataAnalysisService dataAnalysisService;
    private final FileUtils fileUtils;
    private final PandocUtils pandocUtils;

    /**
     * 仅执行 NL2SQL + 查询（不生成报告）
     *
     * @param question 用户问题
     * @param topK     知识库检索数量
     * @return 查询结果
     */
    public QueryResponse query(String question, int topK) {
        log.info("收到查询请求: {}", question);

        QueryResponse response = new QueryResponse();
        response.setQuestion(question);

        try {
            // 1. NL2SQL
            String sql = nl2SqlService.convertToSql(question, topK);
            response.setSql(sql);

            // 2. 执行查询
            String queryResult = dataAnalysisService.executeQuery(sql);
            response.setQueryResult(queryResult);

            response.setSuccess(true);

        } catch (Exception e) {
            log.error("查询失败", e);
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    /**
     * 生成完整的数据分析报告
     * 流程：NL2SQL → 执行查询 → 生成图表 → 生成报告 → 保存文件 → 转换 Word
     *
     * @param request 报告生成请求
     * @return 完整的分析报告响应
     */
    public ReportResponse generateReport(ReportRequest request) {
        log.info("收到报告生成请求，问题: {}", request.getQuestion());

        ReportResponse response = new ReportResponse();
        response.setQuestion(request.getQuestion());

        try {
            // 1. NL2SQL - 将问题转换为 SQL
            log.info("步骤 1: NL2SQL 转换");
            String sql = nl2SqlService.convertToSql(request.getQuestion(), request.getTopK());
            response.setSql(sql);
            log.info("生成的 SQL: {}", sql);

            // 2. 执行 SQL 查询
            log.info("步骤 2: 执行 SQL 查询");
            String queryResult = dataAnalysisService.executeQuery(sql);
            response.setQueryResult(queryResult);
            log.debug("查询结果: {}", queryResult);

            // 3. 生成图表
            String chartUrl = null;
            if (request.isGenerateChart()) {
                log.info("步骤 3: 生成图表");
                String chartType = request.getChartType();
                if (chartType == null || chartType.isEmpty()) {
                    // 智能推荐图表类型
                    chartType = dataAnalysisService.recommendChartType(sql, queryResult);
                    log.info("推荐图表类型: {}", chartType);
                }
                chartUrl = dataAnalysisService.generateChart(queryResult, chartType);
                response.setChartUrl(chartUrl);
                log.info("图表 URL: {}", chartUrl);
            }

            // 4. 生成分析报告
            log.info("步骤 4: 生成分析报告");
            String report = dataAnalysisService.generateReport(
                    request.getQuestion(),
                    sql,
                    queryResult,
                    chartUrl
            );
            response.setReport(report);
            response.setSuccess(true);

            log.info("报告生成完成，总长度: {} 字符", report.length());

            // 5. 保存报告到文件
            String reportFilePath = fileUtils.saveReportToFile(report, request.getQuestion());
            response.setReportFilePath(reportFilePath);
            log.info("报告已保存至: {}", reportFilePath);

            // 6. 转换为 Word 文档（如果请求）
            if (request.isGenerateWord() && reportFilePath != null) {
                log.info("步骤 6: 转换为 Word 文档");
                String wordFilePath = pandocUtils.convertMarkdownToWord(reportFilePath);
                response.setWordFilePath(wordFilePath);
                log.info("Word 文档已生成: {}", wordFilePath);
            }

        } catch (Exception e) {
            log.error("报告生成失败", e);
            response.setSuccess(false);
            response.setErrorMessage("报告生成失败: " + e.getMessage());
        }

        return response;
    }
}
