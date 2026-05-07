package com.edusys.backend.university.dto;

import java.time.LocalDateTime;

public record ServiceTypeResponse(
        Long id,
        String code,
        String name,
        String defaultOffice,
        Integer slaDays,
        Boolean requiresFinanceClearance,
        Boolean requiresAttachment,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
