package com.edusys.backend.university.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceTypeRequest(
        @NotBlank @Size(max = 80) String code,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 120) String defaultOffice,
        @NotNull @Min(1) Integer slaDays,
        Boolean requiresFinanceClearance,
        Boolean requiresAttachment,
        Boolean active
) {}
