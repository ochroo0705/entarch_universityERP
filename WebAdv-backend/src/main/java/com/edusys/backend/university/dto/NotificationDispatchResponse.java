package com.edusys.backend.university.dto;

public record NotificationDispatchResponse(
        long notifications,
        UniversityIntegrationRunResponse integrationRun
) {}
