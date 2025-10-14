package com.sdecloud.dubhe.ai.report.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 文件操作工具类
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Component
public class FileUtils {
    
    private static final String REPORT_DIR = "report-result";
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * 确保报告目录存在
     */
    public void ensureReportDirectory() {
        try {
            Files.createDirectories(Paths.get(REPORT_DIR));
        } catch (IOException e) {
            log.warn("创建报告目录失败: {}", e.getMessage());
        }
    }
    
    /**
     * 保存报告到文件
     *
     * @param report   报告内容（Markdown 格式）
     * @param question 用户问题（用于生成文件名）
     * @return 报告文件路径
     */
    public String saveReportToFile(String report, String question) {
        try {
            // 生成文件名：时间戳_问题摘要.md
            String timestamp = LocalDateTime.now().format(FILE_NAME_FORMATTER);
            String questionPrefix = sanitizeFileName(question);
            String fileName = String.format("%s_%s.md", timestamp, questionPrefix);
            
            // 构建完整路径
            Path reportPath = Paths.get(REPORT_DIR, fileName);
            
            // 写入文件
            Files.writeString(reportPath, report);
            
            log.info("报告已成功保存: {}", reportPath.toAbsolutePath());
            return reportPath.toString();
            
        } catch (IOException e) {
            log.error("保存报告失败", e);
            return null;
        }
    }
    
    /**
     * 清理文件名，移除非法字符
     *
     * @param input 原始字符串
     * @return 清理后的文件名
     */
    private String sanitizeFileName(String input) {
        if (input == null || input.isEmpty()) {
            return "report";
        }
        
        // 只保留中文、字母、数字、下划线、连字符
        String sanitized = input.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9_-]", "_");
        
        // 限制长度为 30 个字符
        if (sanitized.length() > 30) {
            sanitized = sanitized.substring(0, 30);
        }
        
        return sanitized;
    }
}
