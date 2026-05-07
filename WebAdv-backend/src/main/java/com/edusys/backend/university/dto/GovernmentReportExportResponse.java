package com.edusys.backend.university.dto;

public record GovernmentReportExportResponse(
        String reportPeriod,
        long reportRows,
        UniversityIntegrationRunResponse integrationRun
) {}
