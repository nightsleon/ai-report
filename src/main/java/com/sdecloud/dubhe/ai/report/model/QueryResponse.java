package com.sdecloud.dubhe.ai.report.model;

import lombok.Data;

/**
 * 查询响应
 *
 * @author liangjun
 * @since 2025-10-14
 */
@Data
public class QueryResponse {
    /** 是否成功 */
    private boolean success;

    /** 用户问题 */
    private String question;

    /** 生成的 SQL */
    private String sql;

    /** 查询结果 */
    private String queryResult;

    /** 错误信息 */
    private String errorMessage;
}
