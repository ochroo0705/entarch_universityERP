package com.edusys.backend.university.dto;

import jakarta.validation.constraints.NotBlank;

public record GraduationClearanceRequest(
        @NotBlank String programName
) {}
