package com.sdecloud.dubhe.ai.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 数据分析服务
 * 负责执行 SQL 查询、生成图表、生成分析报告
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Service
public class DataAnalysisService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final SystemPromptTemplate reportSystemPromptTemplate;
    private final PromptTemplate reportUserPromptTemplate;

    public DataAnalysisService(ChatModel chatModel, ToolCallbackProvider tools,
                              @Value("classpath:prompts/report-analyst-system-prompt.txt") Resource reportSystemPromptResource,
                              @Value("classpath:prompts/report-analyst-user-prompt.txt") Resource reportUserPromptResource) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .defaultToolCallbacks(tools)
                .build();
        this.objectMapper = new ObjectMapper();
        
        // 初始化 Prompt 模板（只创建一次）
        this.reportSystemPromptTemplate = new SystemPromptTemplate(reportSystemPromptResource);
        this.reportUserPromptTemplate = new PromptTemplate(reportUserPromptResource);
    }

    /**
     * 执行 SQL 查询（通过 MCP mysqlDataQuery 工具）
     * MCP 工具会自动被大模型调用
     *
     * @param sql SQL 查询语句
     * @return 查询结果（JSON 格式）
     */
    public String executeQuery(String sql) {
        log.info("执行 SQL 查询，长度: {} 字符", sql.length());
        log.debug("SQL: {}", sql);

        // 构建明确的工具调用提示词
        String prompt = String.format("""
                请使用 mysqlDataQuery 工具执行以下 SQL 查询：
                
                SQL: %s
                
                请调用 mysqlDataQuery 工具，传入参数：{"sql": "%s"}
                然后返回查询结果。
                """, sql, sql);

        log.debug("执行工具调用提示: {}", prompt);

        // ChatClient 会自动检测并调用可用的 MCP 工具
        String result = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        log.info("SQL 查询执行完成，结果长度: {} 字符", result.length());
        log.debug("查询结果: {}", result);
        return result;
    }

    /**
     * 生成图表（通过 MCP antvChart 工具）
     * MCP 工具会自动被大模型调用
     *
     * @param queryResult 查询结果
     * @param chartType   图表类型（bar/line/pie等）
     * @return 图表 URL
     */
    public String generateChart(String queryResult, String chartType) {
        log.info("生成图表，类型: {}", chartType);

        // 构建明确的工具调用提示词
        String prompt = String.format("""
                请使用 antvChart 工具生成图表：
                
                图表类型: %s
                数据: %s
                
                请调用 antvChart 工具，传入参数：{"chartType": "%s", "data": "%s"}
                然后返回图表 URL。
                """, chartType, queryResult, chartType, queryResult);

        log.debug("图表生成工具调用提示: {}", prompt);

        // ChatClient 会自动检测并调用可用的 MCP 工具
        String chartUrl = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        log.info("图表生成完成: {}", chartUrl);
        return chartUrl;
    }

    /**
     * 生成数据分析报告
     *
     * @param question    用户原始问题
     * @param sql         执行的 SQL
     * @param queryResult 查询结果
     * @param chartUrl    图表 URL
     * @return Markdown 格式的分析报告
     */
    public String generateReport(String question, String sql, String queryResult, String chartUrl) {
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
        return report;
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

    /**
     * 智能推荐图表类型
     *
     * @param sql         SQL 语句
     * @param queryResult 查询结果
     * @return 推荐的图表类型
     */
    public String recommendChartType(String sql, String queryResult) {
        String sqlLower = sql.toLowerCase();

        // 基于 SQL 关键字推荐图表类型
        if (sqlLower.contains("sum") || sqlLower.contains("count")) {
            if (sqlLower.contains("date_format") || sqlLower.contains("month") || sqlLower.contains("year")) {
                return "line"; // 时间趋势用折线图
            }
            return "bar"; // 聚合数据用柱状图
        } else if (sqlLower.contains("ratio") || sqlLower.contains("percentage")) {
            return "pie"; // 占比数据用饼图
        } else if (sqlLower.contains("order by") && sqlLower.contains("limit")) {
            return "bar"; // 排行数据用柱状图
        }

        // 默认柱状图
        return "bar";
    }

}

