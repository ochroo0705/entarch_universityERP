package com.edusys.backend.university.dto;

import java.time.LocalDateTime;

public record UniversityErpEventLogResponse(
        Long id,
        String module,
        String action,
        String entityType,
        Long entityId,
        Long actorUserId,
        String actorName,
        Long studentId,
        String studentName,
        String details,
        LocalDateTime createdAt
) {}
