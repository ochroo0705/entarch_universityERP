package com.edusys.backend.university.dto;

public record LmsRosterExportResponse(
        long rosterRows,
        UniversityIntegrationRunResponse integrationRun
) {}
