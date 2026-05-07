package com.edusys.backend.dto;

import java.util.List;

/**
 * Treat a teaching-assignment id as a "group" key: teacher + subject + academicYear + semester.
 * This endpoint lists all classes taught under that grouping.
 */
public record TeachingAssignmentGroupDTO(
        Long id,
        TeacherDTO teacher,
        SubjectDTO subject,
        String academicYear,
        Integer semester,
        Boolean isActive,
        List<ClassSummaryDTO> classes
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
            String subjectCode,
            Integer gradeLevel,
            Integer hoursPerWeek
    ) {}

    /**
     * Each entry includes the concrete teachingAssignmentId for that class.
     */
    public record ClassSummaryDTO(
            Long teachingAssignmentId,
            Long classId,
            String className,
            Integer grade,
            String section,
            String roomNumber,
            String academicYear
    ) {}
}
