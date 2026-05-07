package com.edusys.backend.dto;

public record TeachingAssignmentListItemDTO(
        Long id,
        TeacherDTO teacher,
        SubjectDTO subject,
        ClassInfoDTO classInfo,
        String academicYear,
        Integer semester,
        Boolean isActive
) {
    public record TeacherDTO(
            Long id,
            String username,
            String firstName,
            String lastName
    ) {}

    public record SubjectDTO(
            Long id,
            String name,
            String subjectNameMn,
            String subjectCode,
            Integer gradeLevel,
            Integer hoursPerWeek
    ) {}

    public record ClassInfoDTO(
            Long id,
            String className,
            Integer grade,
            String section,
            String roomNumber,
            String academicYear
    ) {}
}
