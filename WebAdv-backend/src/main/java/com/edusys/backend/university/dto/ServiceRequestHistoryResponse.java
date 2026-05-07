package com.edusys.backend.university.dto;

import java.time.LocalDateTime;

public record ServiceRequestHistoryResponse(
        Long id,
        Long actorId,
        String actorName,
        String eventType,
        String fromStatus,
        String toStatus,
        String details,
        LocalDateTime createdAt
) {}
