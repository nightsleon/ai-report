package com.sdecloud.dubhe.ai.report.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.sdecloud.dubhe.ai.report.constant.GraphStateKeys;
import com.sdecloud.dubhe.ai.report.util.FileUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 输出MD文档节点
 * 负责生成分析报告并保存为Markdown文件
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Component
public class ReportGenerateNode implements NodeAction {

    private final ChatClient chatClient;
    private final FileUtils fileUtils;
    private final ObjectMapper objectMapper;
    private final SystemPromptTemplate reportSystemPromptTemplate;
    private final PromptTemplate reportUserPromptTemplate;

    public ReportGenerateNode(ChatModel chatModel, FileUtils fileUtils,
                              @Value("classpath:prompts/report-analyst-system-prompt.txt") Resource reportSystemPromptResource,
                              @Value("classpath:prompts/report-analyst-user-prompt.txt") Resource reportUserPromptResource) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.fileUtils = fileUtils;
        this.objectMapper = new ObjectMapper();
        
        // 初始化 Prompt 模板（只创建一次）
        this.reportSystemPromptTemplate = new SystemPromptTemplate(reportSystemPromptResource);
        this.reportUserPromptTemplate = new PromptTemplate(reportUserPromptResource);
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        log.info("执行报告生成节点");

        String question = state.value(GraphStateKeys.QUESTION, "");
        String sql = state.value(GraphStateKeys.SQL, "");
        String queryResult = state.value(GraphStateKeys.QUERY_RESULT, "");
        String chartUrl = state.value(GraphStateKeys.CHART_URL, "");
        Boolean sqlExecuteSuccess = state.value(GraphStateKeys.SQL_EXECUTE_SUCCESS, false);

        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("问题不能为空");
        }

        if (!sqlExecuteSuccess) {
            throw new IllegalStateException("SQL执行失败，无法生成报告");
        }

        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }

        if (queryResult == null || queryResult.trim().isEmpty()) {
            throw new IllegalArgumentException("查询结果不能为空");
        }


        try {
            log.info("开始生成数据分析报告");

            // 确保所有参数都有非 null 值
            String safeQuestion = question != null ? question : "";
            String safeSql = sql != null ? sql : "";
            String safeQueryResult = formatQueryResult(queryResult != null ? queryResult : "{}");
            String safeChartUrl = chartUrl != null && !chartUrl.trim().isEmpty() ? chartUrl : "暂无图表";

            // 1. 创建系统 Prompt（数据分析师角色）
            Message systemMessage = reportSystemPromptTemplate.createMessage();

            // 2. 创建用户 Prompt（问题 + SQL + 结果 + 图表）
            Map<String, Object> params = Map.of(
                    "question", safeQuestion,
                    "sql", safeSql,
                    "queryResult", safeQueryResult,
                    "chartUrl", safeChartUrl
            );

            log.debug("Prompt 参数: {}", params);
            Message userMessage = reportUserPromptTemplate.createMessage(params);

            // 3. 调用大模型生成报告
            log.debug("调用大模型生成分析报告...");
            String report = chatClient.prompt()
                    .messages(systemMessage, userMessage)
                    .call()
                    .content();

            log.info("分析报告生成完成，长度: {} 字符", report.length());

            // 保存报告到文件
            String reportFilePath = fileUtils.saveReportToFile(report, question);
            log.info("报告已保存至: {}", reportFilePath);

            return Map.of(
                GraphStateKeys.REPORT, report,
                GraphStateKeys.REPORT_FILE_PATH, reportFilePath,
                GraphStateKeys.REPORT_GENERATE_SUCCESS, true
            );

        } catch (Exception e) {
            log.error("报告生成失败", e);
            return Map.of(
                GraphStateKeys.REPORT_GENERATE_SUCCESS, false,
                GraphStateKeys.REPORT_GENERATE_ERROR, e.getMessage()
            );
        }
    }

    /**
     * 格式化查询结果，使其更易读
     *
     * @param queryResult 原始查询结果
     * @return 格式化后的结果
     */
    private String formatQueryResult(String queryResult) {
        try {
            // 尝试解析 JSON 并格式化
            Object jsonObject = objectMapper.readValue(queryResult, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            log.debug("查询结果不是 JSON 格式，直接返回原始内容");
            return queryResult;
        }
    }
}
