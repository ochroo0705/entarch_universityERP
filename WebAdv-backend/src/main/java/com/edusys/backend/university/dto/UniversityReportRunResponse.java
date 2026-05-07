package com.edusys.backend.university.dto;

import java.time.LocalDateTime;

public record UniversityReportRunResponse(
        Long id,
        String reportKey,
        String reportName,
        String category,
        String status,
        String filters,
        String snapshotPayload,
        long rowCount,
        LocalDateTime generatedAt
) {}
