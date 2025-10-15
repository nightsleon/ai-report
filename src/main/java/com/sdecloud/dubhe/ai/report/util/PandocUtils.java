package com.sdecloud.dubhe.ai.report.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Pandoc 转换工具类
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Slf4j
@Component
public class PandocUtils {
    
    private static final String PANDOC_PATH = "/opt/homebrew/bin/pandoc";
    
    /**
     * 将 Markdown 文件转换为 Word 文档
     *
     * @param markdownFilePath Markdown 文件路径
     * @return Word 文件路径，转换失败返回 null
     */
    public static String convertMarkdownToWord(String markdownFilePath) {
        try {
            // 生成 Word 文件路径（替换 .md 为 .docx）
            String wordFilePath = markdownFilePath.replaceAll("\\.md$", ".docx");
            
            // 构建 pandoc 命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                    PANDOC_PATH,
                    markdownFilePath,
                    "-o", wordFilePath,
                    "--standalone"
            );
            
            // 设置工作目录
            processBuilder.directory(new java.io.File("."));
            
            // 合并错误输出到标准输出
            processBuilder.redirectErrorStream(true);
            
            log.debug("执行 Pandoc 命令: {}", String.join(" ", processBuilder.command()));
            
            // 启动进程
            Process process = processBuilder.start();
            
            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.debug("Pandoc output: {}", line);
                }
            }
            
            // 等待进程完成
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("Markdown 转 Word 成功: {} -> {}", markdownFilePath, wordFilePath);
                return wordFilePath;
            } else {
                log.error("Pandoc 转换失败，退出码: {}, 输出: {}", exitCode, output);
                return null;
            }
            
        } catch (IOException e) {
            log.error("执行 Pandoc 命令失败", e);
            return null;
        } catch (InterruptedException e) {
            log.error("Pandoc 进程被中断", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
