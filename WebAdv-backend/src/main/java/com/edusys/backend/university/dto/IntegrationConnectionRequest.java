package com.edusys.backend.university.dto;

import jakarta.validation.constraints.NotBlank;

public record IntegrationConnectionRequest(
        @NotBlank String integrationKey,
        @NotBlank String displayName,
        String endpointUrl,
        String adapterMode,
        String authType,
        String secretRef,
        Boolean enabled
) {}
