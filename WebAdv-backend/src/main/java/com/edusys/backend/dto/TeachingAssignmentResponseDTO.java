package com.edusys.backend.dto;

public record TeachingAssignmentResponseDTO(
        Long id,
        Long teacherId,
        String teacherUsername,
        Long subjectId,
        String subjectName,
        Long classId,
        String className,
        String academicYear,
        Integer semester,
        Boolean isActive
) {}
