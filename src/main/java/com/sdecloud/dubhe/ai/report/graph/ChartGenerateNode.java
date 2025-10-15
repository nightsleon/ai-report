package com.sdecloud.dubhe.ai.report.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 绘制图表节点
 * 负责根据查询结果生成图表（通过MCP antvChart工具）
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Component
public class ChartGenerateNode implements NodeAction {

    private final ChatClient chatClient;

    public ChartGenerateNode(ChatModel chatModel, ToolCallbackProvider tools) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(tools)
                .build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        log.info("执行图表生成节点");

        String sql = state.value("sql", "");
        String queryResult = state.value("queryResult", "");
        Boolean sqlExecuteSuccess = state.value("sql_execute_success", false);
        Boolean generateChart = state.value("generateChart", true);

        if (!sqlExecuteSuccess) {
            throw new IllegalStateException("SQL执行失败，无法生成图表");
        }

        if (queryResult == null || queryResult.trim().isEmpty()) {
            throw new IllegalArgumentException("查询结果不能为空");
        }

        // 如果不需要生成图表，直接跳过
        if (!generateChart) {
            log.info("跳过图表生成");
            return Map.of(
                    "chart_generate_success", true,
                    "chartUrl", null
            );
        }

        try {
            // 智能推荐图表类型
            String chartType = recommendChartType(sql, queryResult);
            log.info("推荐图表类型: {}", chartType);

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

            log.info("图表生成成功: {}", chartUrl);

            return Map.of(
                    "chartUrl", chartUrl,
                    "chartType", chartType,
                    "chart_generate_success", true
            );

        } catch (Exception e) {
            log.error("图表生成失败", e);
            // 图表生成失败不应该中断整个流程，只记录错误
            log.warn("图表生成失败，继续执行后续步骤: {}", e.getMessage());
            return Map.of(
                    "chart_generate_success", false,
                    "chart_generate_error", e.getMessage()
            );
        }
    }

    /**
     * 智能推荐图表类型
     *
     * @param sql         SQL 语句
     * @param queryResult 查询结果
     * @return 推荐的图表类型
     */
    private String recommendChartType(String sql, String queryResult) {
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
