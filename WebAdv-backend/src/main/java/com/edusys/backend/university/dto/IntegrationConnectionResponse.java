package com.edusys.backend.university.dto;

public record IntegrationConnectionResponse(
        Long id,
        String integrationKey,
        String displayName,
        String endpointUrl,
        String adapterMode,
        String authType,
        String secretRef,
        Boolean enabled,
        String lastStatus
) {}
