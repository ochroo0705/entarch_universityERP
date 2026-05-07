package com.edusys.backend.dto;

import java.util.List;

public record StudentGPADTO(
        Long studentId,
        String studentName,
        String className,
        Integer quarter,
        String academicYear,
        double gpa,
        List<SubjectGrade> subjectGrades,
        String performance
) {
    public record SubjectGrade(
            String subjectName,
            Integer gradeValue,
            String gradeType
    ) {}

    public enum Performance {
        EXCELLENT,    // GPA >= 90
        GOOD,         // GPA >= 80
        SATISFACTORY, // GPA >= 70
        NEEDS_IMPROVEMENT // GPA < 70
    }
}