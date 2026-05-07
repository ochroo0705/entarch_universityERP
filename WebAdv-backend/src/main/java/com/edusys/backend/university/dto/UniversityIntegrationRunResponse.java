package com.edusys.backend.university.dto;

import java.time.LocalDateTime;

public record UniversityIntegrationRunResponse(
        Long id,
        String key,
        String name,
        String direction,
        String status,
        String payload,
        String resultMessage,
        Integer retryCount,
        String errorMessage,
        LocalDateTime exchangedAt
) {}
