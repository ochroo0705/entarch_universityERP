package com.edusys.backend.university.dto;

public record IntegrationSmokeTestResponse(
        String key,
        String adapterMode,
        String authType,
        String status,
        String message,
        Boolean secretResolved
) {}
