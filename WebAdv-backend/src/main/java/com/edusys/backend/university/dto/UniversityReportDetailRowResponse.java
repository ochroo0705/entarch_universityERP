package com.edusys.backend.university.dto;

import java.math.BigDecimal;

public record UniversityReportDetailRowResponse(
        String reportKey,
        String entityType,
        Long entityId,
        String primaryLabel,
        String secondaryLabel,
        String status,
        BigDecimal amount,
        String details
) {}
