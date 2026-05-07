package com.edusys.backend.ai.service.provider;

public class ParentMessageGenerationException extends RuntimeException {
    private final String errorCode;
    private final String providerName;

    public ParentMessageGenerationException(String errorCode, String providerName, String message) {
        super(message);
        this.errorCode = errorCode;
        this.providerName = providerName;
    }

    public ParentMessageGenerationException(String errorCode, String providerName, String message, Throwable cause) {
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
