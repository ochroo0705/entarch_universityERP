package com.edusys.backend.university.dto;

public record CoursePrerequisiteResponse(
        Long id,
        Long subjectId,
        String subjectName,
        String subjectCode,
        Long prerequisiteSubjectId,
        String prerequisiteName,
        String prerequisiteCode,
        String groupCode
) {}
