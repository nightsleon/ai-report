package com.sdecloud.dubhe.ai.report.exception;

/**
 * 报告生成异常
 * 用于报告生成流程中的业务异常
 *
 * @author liangjun
 * @since 2025-10-15
 */
public class ReportGenerationException extends RuntimeException {

    private final String errorCode;
    private final String step;

    public ReportGenerationException(String message) {
        super(message);
        this.errorCode = "REPORT_ERROR";
        this.step = null;
    }

    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "REPORT_ERROR";
        this.step = null;
    }

    public ReportGenerationException(String errorCode, String step, String message) {
        super(String.format("[%s] %s: %s", errorCode, step, message));
        this.errorCode = errorCode;
        this.step = step;
    }

    public ReportGenerationException(String errorCode, String step, String message, Throwable cause) {
        super(String.format("[%s] %s: %s", errorCode, step, message), cause);
        this.errorCode = errorCode;
        this.step = step;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getStep() {
        return step;
    }
}

