package com.edusys.backend.dto;

import com.edusys.backend.model.Homework;

import java.time.LocalDate;

public record HomeworkCreateDto(
        Long teachingAssignmentId,
        String title,
        String description,
        LocalDate dueDate,
        Integer maxScore,
        Homework.Type type,
        String attachmentUrl
) {}
