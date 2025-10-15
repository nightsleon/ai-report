package com.sdecloud.dubhe.ai.report.constant;

/**
 * Graph状态键常量
 * 定义Graph节点间传递数据的键名
 *
 * @author liangjun
 * @since 2025-10-15
 */
public final class GraphStateKeys {

    private GraphStateKeys() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ========== 输入参数 ==========
    /** 用户问题 */
    public static final String QUESTION = "question";
    /** 知识库检索数量 */
    public static final String TOP_K = "topK";
    /** 是否生成图表 */
    public static final String GENERATE_CHART = "generateChart";
    /** 是否生成Word文档 */
    public static final String GENERATE_WORD = "generateWord";

    // ========== NL2SQL结果 ==========
    /** 生成的SQL */
    public static final String SQL = "sql";
    /** NL2SQL是否成功 */
    public static final String NL2SQL_SUCCESS = "nl2sql_success";
    /** NL2SQL错误信息 */
    public static final String NL2SQL_ERROR = "nl2sql_error";

    // ========== SQL执行结果 ==========
    /** 查询结果 */
    public static final String QUERY_RESULT = "queryResult";
    /** SQL执行是否成功 */
    public static final String SQL_EXECUTE_SUCCESS = "sql_execute_success";
    /** SQL执行错误信息 */
    public static final String SQL_EXECUTE_ERROR = "sql_execute_error";

    // ========== 图表生成结果 ==========
    /** 图表URL */
    public static final String CHART_URL = "chartUrl";
    /** 图表类型 */
    public static final String CHART_TYPE = "chartType";
    /** 图表生成是否成功 */
    public static final String CHART_GENERATE_SUCCESS = "chart_generate_success";
    /** 图表生成错误信息 */
    public static final String CHART_GENERATE_ERROR = "chart_generate_error";

    // ========== 报告生成结果 ==========
    /** 报告内容 */
    public static final String REPORT = "report";
    /** 报告文件路径 */
    public static final String REPORT_FILE_PATH = "reportFilePath";
    /** 报告生成是否成功 */
    public static final String REPORT_GENERATE_SUCCESS = "report_generate_success";
    /** 报告生成错误信息 */
    public static final String REPORT_GENERATE_ERROR = "report_generate_error";

    // ========== Word转换结果 ==========
    /** Word文件路径 */
    public static final String WORD_FILE_PATH = "wordFilePath";
    /** Word转换是否成功 */
    public static final String WORD_CONVERT_SUCCESS = "word_convert_success";
    /** Word转换错误信息 */
    public static final String WORD_CONVERT_ERROR = "word_convert_error";
}

