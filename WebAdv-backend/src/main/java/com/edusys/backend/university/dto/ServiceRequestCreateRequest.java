package com.edusys.backend.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceRequestCreateRequest(
        @NotNull Long studentId,
        @NotBlank @Size(max = 120) String requestType,
        @Size(max = 2000) String description
) {}
