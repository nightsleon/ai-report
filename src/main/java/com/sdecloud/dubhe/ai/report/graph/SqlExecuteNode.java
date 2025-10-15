package com.sdecloud.dubhe.ai.report.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.sdecloud.dubhe.ai.report.constant.GraphStateKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * SQL执行节点
 * 负责执行SQL查询并获取结果（通过MCP mysqlDataQuery工具）
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Component
public class SqlExecuteNode implements NodeAction {

    private final ChatClient chatClient;

    public SqlExecuteNode(ChatModel chatModel, ToolCallbackProvider tools) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(tools)
                .build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        log.info("执行SQL查询节点");

        String sql = state.value(GraphStateKeys.SQL, "");
        Boolean nl2sqlSuccess = state.value(GraphStateKeys.NL2SQL_SUCCESS, false);

        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL语句不能为空");
        }

        if (!nl2sqlSuccess) {
            throw new IllegalStateException("NL2SQL转换失败，无法执行SQL");
        }

        log.info("执行 SQL 查询，长度: {} 字符", sql.length());
        log.debug("SQL: {}", sql);

        try {
            // 构建明确的工具调用提示词
            String prompt = String.format("""
                    请使用 mysqlDataQuery 工具执行以下 SQL 查询：
                    
                    SQL: %s
                    
                    请调用 mysqlDataQuery 工具，传入参数：{"sql": "%s"}
                    然后返回查询结果。
                    """, sql, sql);

            log.debug("执行工具调用提示: {}", prompt);

            // ChatClient 会自动检测并调用可用的 MCP 工具
            String queryResult = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("SQL执行成功，结果长度: {} 字符", queryResult.length());
            log.debug("查询结果: {}", queryResult);

            return Map.of(
                GraphStateKeys.QUERY_RESULT, queryResult,
                GraphStateKeys.SQL_EXECUTE_SUCCESS, true
            );

        } catch (Exception e) {
            log.error("SQL执行失败", e);
            return Map.of(
                GraphStateKeys.SQL_EXECUTE_SUCCESS, false,
                GraphStateKeys.SQL_EXECUTE_ERROR, e.getMessage()
            );
        }
    }
}
