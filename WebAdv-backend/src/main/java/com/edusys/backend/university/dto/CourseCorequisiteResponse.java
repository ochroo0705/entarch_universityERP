package com.edusys.backend.university.dto;

public record CourseCorequisiteResponse(
        Long id,
        Long subjectId,
        String subjectName,
        String subjectCode,
        Long corequisiteSubjectId,
        String corequisiteSubjectName,
        String corequisiteSubjectCode
) {}
