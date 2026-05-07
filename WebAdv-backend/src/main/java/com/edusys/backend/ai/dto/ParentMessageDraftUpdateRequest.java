package com.edusys.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record ParentMessageDraftUpdateRequest(
        @NotBlank(message = "currentSubject is required")
        String currentSubject,
        @NotBlank(message = "currentMessageBody is required")
        String currentMessageBody
) {
}
