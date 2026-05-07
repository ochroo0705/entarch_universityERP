package com.edusys.backend.ai.service.provider;

public class AnalyticsSummaryGenerationException extends RuntimeException {

    private final String errorCode;
    private final String providerName;

    public AnalyticsSummaryGenerationException(String errorCode, String providerName, String message) {
        super(message);
        this.errorCode = errorCode;
        this.providerName = providerName;
    }

    public AnalyticsSummaryGenerationException(String errorCode, String providerName, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.providerName = providerName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getProviderName() {
        return providerName;
    }
}
