package com.edusys.backend.university.dto;

import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(
        @NotBlank String code,
        @NotBlank String name,
        Boolean active
) {}
