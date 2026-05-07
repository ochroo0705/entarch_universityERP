package com.edusys.backend.university.dto;

public record UniversityIntegrationStatusResponse(
        String key,
        String name,
        String direction,
        String status,
        String lastExchange,
        String payload
) {}
