package com.sdecloud.dubhe.ai.report.model;

import lombok.Data;

/**
 * 报告生成请求
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Data
public class ReportRequest {
    /** 用户问题 */
    private String question;

    /** 知识库检索数量，默认 3 */
    private int topK = 3;

    /** 是否生成图表，默认 true */
    private boolean generateChart = true;

    /** 图表类型（bar/line/pie等），null 则自动推荐 */
    private String chartType;
    
    /** 是否生成 Word 文档，默认 true */
    private boolean generateWord = true;
}
