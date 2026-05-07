package com.edusys.backend.ai.dto;

import java.util.List;

public record RiskAccessScopeResponse(
        List<AccessibleClassResponse> classes,
        List<AiStudentOptionResponse> students
) {
    public record AccessibleClassResponse(
            Long classId,
            String className,
            Integer gradeLevel
    ) {
    }
}
