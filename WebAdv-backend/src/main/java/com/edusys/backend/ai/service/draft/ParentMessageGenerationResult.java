package com.edusys.backend.ai.service.draft;

public record ParentMessageGenerationResult(
        String subject,
        String messageBody,
        String providerName,
        String providerModel,
        String providerRequestId,
        String outputRedactedJson,
        boolean placeholder
) {
}
