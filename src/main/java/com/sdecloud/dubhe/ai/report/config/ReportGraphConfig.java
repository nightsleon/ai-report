package com.sdecloud.dubhe.ai.report.config;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.sdecloud.dubhe.ai.report.graph.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * 报告生成Graph配置
 * 定义报告生成的完整流程：NL2SQL → SQL执行 → 图表生成 → 报告生成 → Word转换
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReportGraphConfig {

    private final Nl2SqlNode nl2SqlNode;
    private final SqlExecuteNode sqlExecuteNode;
    private final ChartGenerateNode chartGenerateNode;
    private final ReportGenerateNode reportGenerateNode;
    private final WordConvertNode wordConvertNode;

    /**
     * 创建键策略工厂Bean
     * 定义状态键的更新策略
     */
    @Bean
    public KeyStrategyFactory keyStrategyFactory() {
        return () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            
            // 输入参数
            keyStrategyHashMap.put("question", new ReplaceStrategy());
            keyStrategyHashMap.put("topK", new ReplaceStrategy());
            keyStrategyHashMap.put("generateChart", new ReplaceStrategy());
            keyStrategyHashMap.put("generateWord", new ReplaceStrategy());
            
            // NL2SQL结果
            keyStrategyHashMap.put("sql", new ReplaceStrategy());
            keyStrategyHashMap.put("nl2sql_success", new ReplaceStrategy());
            keyStrategyHashMap.put("nl2sql_error", new ReplaceStrategy());
            
            // SQL执行结果
            keyStrategyHashMap.put("queryResult", new ReplaceStrategy());
            keyStrategyHashMap.put("sql_execute_success", new ReplaceStrategy());
            keyStrategyHashMap.put("sql_execute_error", new ReplaceStrategy());
            
            // 图表生成结果
            keyStrategyHashMap.put("chartUrl", new ReplaceStrategy());
            keyStrategyHashMap.put("chartType", new ReplaceStrategy());
            keyStrategyHashMap.put("chart_generate_success", new ReplaceStrategy());
            keyStrategyHashMap.put("chart_generate_error", new ReplaceStrategy());
            
            // 报告生成结果
            keyStrategyHashMap.put("report", new ReplaceStrategy());
            keyStrategyHashMap.put("reportFilePath", new ReplaceStrategy());
            keyStrategyHashMap.put("report_generate_success", new ReplaceStrategy());
            keyStrategyHashMap.put("report_generate_error", new ReplaceStrategy());
            
            // Word转换结果
            keyStrategyHashMap.put("wordFilePath", new ReplaceStrategy());
            keyStrategyHashMap.put("word_convert_success", new ReplaceStrategy());
            keyStrategyHashMap.put("word_convert_error", new ReplaceStrategy());
            
            return keyStrategyHashMap;
        };
    }

    /**
     * 创建报告生成Graph
     * 流程：NL2SQL → SQL执行 → 图表生成 → 报告生成 → Word转换
     */
    @Bean
    public StateGraph reportGraph(KeyStrategyFactory keyStrategyFactory) throws GraphStateException {
        log.info("创建报告生成Graph");

        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                // 添加节点
                .addNode("nl2sql", AsyncNodeAction.node_async(nl2SqlNode))
                .addNode("sqlExecute", AsyncNodeAction.node_async(sqlExecuteNode))
                .addNode("chartGenerate", AsyncNodeAction.node_async(chartGenerateNode))
                .addNode("reportGenerate", AsyncNodeAction.node_async(reportGenerateNode))
                .addNode("wordConvert", AsyncNodeAction.node_async(wordConvertNode))
                // 定义节点之间的连接关系
                .addEdge(StateGraph.START, "nl2sql")
                .addEdge("nl2sql", "sqlExecute")
                .addEdge("sqlExecute", "chartGenerate")
                .addEdge("chartGenerate", "reportGenerate")
                .addEdge("reportGenerate", "wordConvert")
                .addEdge("wordConvert", StateGraph.END);

        // 添加 PlantUML 打印
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML,
                "数据分析报告生成流程");
        log.info("\n=== 数据分析报告生成流程 UML ===");
        log.info(representation.content());
        log.info("==================================\n");

        return stateGraph;
    }

    /**
     * 创建编译后的Graph Bean（性能优化）
     * 避免每次调用都重新编译Graph
     */
    @Bean
    public CompiledGraph compiledReportGraph(StateGraph reportGraph) throws GraphStateException {
        log.info("编译报告生成Graph（一次性编译，提升性能）");
        return reportGraph.compile();
    }

    /**
     * 创建查询专用Graph
     * 流程：NL2SQL → SQL执行（仅此两步）
     */
    @Bean
    public StateGraph queryGraph(KeyStrategyFactory keyStrategyFactory) throws GraphStateException {
        log.info("创建查询专用Graph");

        StateGraph stateGraph = new StateGraph(keyStrategyFactory)
                // 只添加查询相关的节点
                .addNode("nl2sql", AsyncNodeAction.node_async(nl2SqlNode))
                .addNode("sqlExecute", AsyncNodeAction.node_async(sqlExecuteNode))
                // 定义简单的流程
                .addEdge(StateGraph.START, "nl2sql")
                .addEdge("nl2sql", "sqlExecute")
                .addEdge("sqlExecute", StateGraph.END);

        // 添加 PlantUML 打印
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML,
                "查询流程");
        log.info("\n=== 查询流程 UML ===");
        log.info(representation.content());
        log.info("====================\n");

        return stateGraph;
    }

    /**
     * 创建编译后的查询Graph Bean（性能优化）
     */
    @Bean
    public CompiledGraph compiledQueryGraph(StateGraph queryGraph) throws GraphStateException {
        log.info("编译查询Graph（一次性编译，提升性能）");
        return queryGraph.compile();
    }
}
