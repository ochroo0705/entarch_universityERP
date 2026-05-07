package com.edusys.backend.dto;

import com.edusys.backend.model.HomeworkSubmission;

import java.time.LocalDateTime;

public record HomeworkSubmissionResponseDto(
        Long id,
        Long homeworkId,
        Long studentId,
        String submissionText,
        String attachmentUrl,
        LocalDateTime submittedAt,
        Integer score,
        String feedback,
        Long gradedById,
        LocalDateTime gradedAt,
        HomeworkSubmission.Status status
) {}
