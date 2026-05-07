package com.edusys.backend.dto;

import com.edusys.backend.model.Homework;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record HomeworkResponseDto(
        Long id,
        Long teachingAssignmentId,
        String subjectName,
        String className,
        String teacherName,
        String title,
        String description,
        LocalDate dueDate,
        Integer maxScore,
        Homework.Type type,
        String attachmentUrl,
        List<HomeworkAttachmentDto> attachments,
        LocalDateTime createdAt,
        HomeworkSubmissionResponseDto submission
) {}
