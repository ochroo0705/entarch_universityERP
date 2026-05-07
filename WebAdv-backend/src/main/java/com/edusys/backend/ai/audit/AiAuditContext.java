package com.edusys.backend.ai.audit;

public record AiAuditContext(
        String requestId,
        String ipAddress,
        String userAgent
) {
}
