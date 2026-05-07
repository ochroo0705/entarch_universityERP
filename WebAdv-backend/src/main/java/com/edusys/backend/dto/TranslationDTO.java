package com.edusys.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TranslationDTO(
        Long id,
        @NotBlank String entityType,
        @NotNull Long entityId,
        @NotBlank String fieldName,
        @NotBlank String locale,
        @NotBlank String value
) {}
