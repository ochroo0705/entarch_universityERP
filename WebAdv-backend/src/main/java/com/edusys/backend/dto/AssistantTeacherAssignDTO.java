package com.edusys.backend.dto;

import jakarta.validation.constraints.NotNull;

public record AssistantTeacherAssignDTO(
        @NotNull Long teacherId
) {}
