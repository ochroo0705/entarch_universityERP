package com.edusys.backend.dto;

import java.util.List;

public record QuarterComparison(
        int quarter,
        double gpa,
        String performance,
        List<SubjectGrade> subjects
) {}
