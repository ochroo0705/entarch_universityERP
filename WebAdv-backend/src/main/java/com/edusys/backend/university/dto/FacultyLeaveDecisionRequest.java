package com.edusys.backend.university.dto;

import jakarta.validation.constraints.NotBlank;

public record FacultyLeaveDecisionRequest(
        @NotBlank String status,
        String decisionNotes
) {}
