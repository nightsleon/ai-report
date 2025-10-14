package com.sdecloud.dubhe.ai.report.model;

import lombok.Data;

/**
 * 报告生成响应
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Data
public class ReportResponse {
    /** 是否成功 */
    private boolean success;

    /** 用户问题 */
    private String question;

    /** 生成的 SQL */
    private String sql;

    /** 查询结果 */
    private String queryResult;

    /** 图表 URL */
    private String chartUrl;

    /** 分析报告（Markdown 格式） */
    private String report;
    
    /** 报告文件路径（Markdown） */
    private String reportFilePath;
    
    /** Word 文档路径 */
    private String wordFilePath;

    /** 错误信息 */
    private String errorMessage;
}
