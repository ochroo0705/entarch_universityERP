package com.edusys.backend.dto;

public record HomeworkSubmissionCreateDto(
        String submissionText,
        String attachmentUrl
) {}
