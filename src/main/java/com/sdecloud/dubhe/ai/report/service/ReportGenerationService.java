package com.sdecloud.dubhe.ai.report.service;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.sdecloud.dubhe.ai.report.model.QueryResponse;
import com.sdecloud.dubhe.ai.report.model.ReportRequest;
import com.sdecloud.dubhe.ai.report.model.ReportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 报告生成服务
 * 负责协调Graph执行报告生成流程
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final CompiledGraph compiledReportGraph;
    private final CompiledGraph compiledQueryGraph;

    /**
     * 仅执行 NL2SQL + 查询（不生成报告）
     * 使用专门的查询Graph：NL2SQL → SQL执行
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
            // 使用查询专用Graph（只执行NL2SQL和SQL执行）
            Map<String, Object> input = new HashMap<>();
            input.put("question", question);
            input.put("topK", topK);

            // 执行查询Graph流程（只有两个节点）
            OverAllState resultState = compiledQueryGraph.invoke(input).orElseThrow(
                    () -> new RuntimeException("查询执行失败，未返回结果")
            );

            Map<String, Object> graphResult = resultState.data();
            String sql = (String) graphResult.get("sql");
            String queryResult = (String) graphResult.get("queryResult");
            Boolean nl2sqlSuccess = (Boolean) graphResult.get("nl2sql_success");
            Boolean sqlExecuteSuccess = (Boolean) graphResult.get("sql_execute_success");

            response.setSql(sql);
            response.setQueryResult(queryResult);

            if (nl2sqlSuccess != null && nl2sqlSuccess &&
                    sqlExecuteSuccess != null && sqlExecuteSuccess) {
                response.setSuccess(true);
                log.info("查询执行成功");
            } else {
                response.setSuccess(false);
                response.setErrorMessage("查询执行失败");
            }

        } catch (Exception e) {
            log.error("查询失败", e);
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    /**
     * 生成完整的数据分析报告（使用Graph）
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
            // 使用Graph执行完整流程
            log.info("使用Graph执行报告生成流程");

            // 创建初始输入
            Map<String, Object> input = new HashMap<>();
            input.put("question", request.getQuestion());
            input.put("topK", request.getTopK());
            input.put("generateChart", request.isGenerateChart());
            input.put("generateWord", request.isGenerateWord());

            // 执行Graph流程
            OverAllState resultState = compiledReportGraph.invoke(input).orElseThrow(
                    () -> new RuntimeException("Graph执行失败，未返回结果")
            );

            log.info("Graph执行完成");
            log.debug("Graph执行结果: {}", resultState.data());

            // 从Graph结果中提取数据
            Map<String, Object> graphResult = resultState.data();
            String sql = (String) graphResult.get("sql");
            String queryResult = (String) graphResult.get("queryResult");
            String chartUrl = (String) graphResult.get("chartUrl");
            String report = (String) graphResult.get("report");
            String reportFilePath = (String) graphResult.get("reportFilePath");
            String wordFilePath = (String) graphResult.get("wordFilePath");

            // 设置响应数据
            response.setSql(sql);
            response.setQueryResult(queryResult);
            response.setChartUrl(chartUrl);
            response.setReport(report);
            response.setReportFilePath(reportFilePath);
            response.setWordFilePath(wordFilePath);

            // 检查各步骤是否成功
            Boolean nl2sqlSuccess = (Boolean) graphResult.get("nl2sql_success");
            Boolean sqlExecuteSuccess = (Boolean) graphResult.get("sql_execute_success");
            Boolean reportGenerateSuccess = (Boolean) graphResult.get("report_generate_success");

            if (nl2sqlSuccess != null && nl2sqlSuccess &&
                    sqlExecuteSuccess != null && sqlExecuteSuccess &&
                    reportGenerateSuccess != null && reportGenerateSuccess) {
                response.setSuccess(true);
                log.info("Graph执行成功，报告生成完成");
                log.info("- SQL: {}", sql);
                log.info("- 报告文件: {}", reportFilePath);
                if (wordFilePath != null) {
                    log.info("- Word文档: {}", wordFilePath);
                }
            } else {
                response.setSuccess(false);
                response.setErrorMessage("Graph执行过程中某些步骤失败");
                log.warn("Graph执行部分失败，nl2sql: {}, sqlExecute: {}, reportGenerate: {}",
                        nl2sqlSuccess, sqlExecuteSuccess, reportGenerateSuccess);
            }

        } catch (Exception e) {
            log.error("报告生成失败", e);
            response.setSuccess(false);
            response.setErrorMessage("报告生成失败: " + e.getMessage());
        }

        return response;
    }
}
