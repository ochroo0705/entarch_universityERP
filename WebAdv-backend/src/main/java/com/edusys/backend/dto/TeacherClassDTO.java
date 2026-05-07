package com.edusys.backend.dto;

public record TeacherClassDTO(
        Long classId,
        String className,
        Integer gradeLevel,
        String subject,
        Integer studentCount
) {}
