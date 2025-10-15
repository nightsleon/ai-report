package com.sdecloud.dubhe.ai.report.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.sdecloud.dubhe.ai.report.util.PandocUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MD转换Word节点
 * 负责将Markdown文件转换为Word文档
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Component
public class WordConvertNode implements NodeAction {

    @Override
    public Map<String, Object> apply(OverAllState state) {
        log.info("执行Word转换节点");

        String reportFilePath = state.value("reportFilePath", "");
        Boolean reportGenerateSuccess = state.value("report_generate_success", false);
        Boolean generateWord = state.value("generateWord", false);

        if (reportFilePath == null || reportFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("报告文件路径不能为空");
        }

        if (!reportGenerateSuccess) {
            throw new IllegalStateException("报告生成失败，无法转换Word");
        }

        // 如果不需要生成Word文档，直接跳过
        if (!generateWord) {
            log.info("跳过Word转换");
            return Map.of(
                "word_convert_success", true,
                "wordFilePath", null
            );
        }

        try {
            // 转换Markdown为Word
            String wordFilePath = PandocUtils.convertMarkdownToWord(reportFilePath);

            if (wordFilePath != null) {
                log.info("Word文档生成成功: {}", wordFilePath);
                return Map.of(
                    "wordFilePath", wordFilePath,
                    "word_convert_success", true
                );
            } else {
                // 转换失败，但不中断流程
                log.warn("Word转换失败，但继续执行");
                return Map.of(
                    "word_convert_success", false,
                    "word_convert_error", "Pandoc转换失败"
                );
            }

        } catch (Exception e) {
            log.error("Word转换失败", e);
            // Word转换失败不应该中断整个流程，只记录错误
            log.warn("Word转换失败，继续执行: {}", e.getMessage());
            return Map.of(
                "word_convert_success", false,
                "word_convert_error", e.getMessage()
            );
        }
    }
}
