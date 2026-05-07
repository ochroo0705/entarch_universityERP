package com.edusys.backend.ai.dto;

import com.edusys.backend.ai.model.AiAuditActionStatus;
import com.edusys.backend.ai.model.AiAuditEventType;
import com.edusys.backend.ai.model.AiEntityType;

import java.time.LocalDateTime;

public record AiAuditLogResponse(
        Long id,
        AiAuditEventType eventType,
        AiEntityType entityType,
        Long entityId,
        Long actorUserId,
        String actorUserName,
        Long targetStudentId,
        String targetStudentName,
        Long targetParentUserId,
        String targetParentUserName,
        String requestId,
        String correlationId,
        AiAuditActionStatus actionStatus,
        String reasonCode,
        String providerName,
        String providerModel,
        String detailsJson,
        String oldValueJson,
        String newValueJson,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
) {
}
