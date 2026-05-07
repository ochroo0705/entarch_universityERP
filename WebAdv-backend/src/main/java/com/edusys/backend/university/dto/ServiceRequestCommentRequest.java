package com.edusys.backend.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServiceRequestCommentRequest(
        @NotBlank @Size(max = 2000) String commentText,
        Boolean internal
) {}
