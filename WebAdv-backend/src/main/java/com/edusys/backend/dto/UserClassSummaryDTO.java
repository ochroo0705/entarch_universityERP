package com.edusys.backend.dto;

public record UserClassSummaryDTO(
        Long studentId,
        Long classId,
        String className,
        Integer grade,
        String section
) {
}
