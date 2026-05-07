package com.edusys.backend.dto;

public record HomeworkGradeBulkDto(
        Long submissionId,
        Integer score,
        String feedback
) {}
