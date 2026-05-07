package com.edusys.backend.dto;

import jakarta.validation.constraints.NotNull;

public record ExamResultPublishRequestDTO(
        @NotNull(message = "Published flag is required")
        Boolean published
) {
}
