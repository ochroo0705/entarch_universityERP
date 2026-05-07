package com.edusys.backend.university.dto;

import com.edusys.backend.university.model.CourseSelectionStatus;

import java.time.LocalDateTime;

public record CourseSelectionResponse(
        Long id,
        Long studentId,
        String studentName,
        Long subjectId,
        String courseName,
        String courseCode,
        String academicYear,
        Integer semester,
        Integer credits,
        CourseSelectionStatus status,
        Long invoiceId,
        String invoiceNumber,
        LocalDateTime selectedAt
) {}
