package com.edusys.backend.dto;

import com.edusys.backend.model.Homework;

import java.time.LocalDate;

/**
 * Create-homework request body without teachingAssignmentId.
 * teachingAssignmentId is supplied via the URL path.
 */
public record HomeworkCreateRequestDto(
        String title,
        String description,
        LocalDate dueDate,
        Integer maxScore,
        Homework.Type type,
        String attachmentUrl
) {
    public HomeworkCreateDto toCreateDto(Long teachingAssignmentId) {
        return new HomeworkCreateDto(
                teachingAssignmentId,
                title,
                description,
                dueDate,
                maxScore,
                type,
                attachmentUrl
        );
    }
}
