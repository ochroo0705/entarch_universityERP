package com.edusys.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ParentMessageDraftCreateRequest(
        @NotNull(message = "studentId is required")
        Long studentId,
        @NotNull(message = "parentUserId is required")
        Long parentUserId,
        Long riskSnapshotId,
        String channel,
        @NotBlank(message = "issueType is required")
        String issueType,
        @Size(max = 1000, message = "teacherNote must be 1000 characters or fewer")
        String teacherNote,
        String toneLabel,
        String languageCode,
        String goalLabel
) {
}
