package com.edusys.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClassCreateDTO(
        @NotBlank String className,
        @NotNull Integer grade,
        @NotBlank String section,
        String roomNumber,
        @NotBlank String academicYear,
        Long homeroomTeacherId
) {}
