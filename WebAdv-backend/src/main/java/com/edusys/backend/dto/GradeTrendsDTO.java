package com.edusys.backend.dto;

import java.util.List;

public record GradeTrendsDTO(
        Long studentId,
        String studentName,
        String className,
        List<QuarterTrend> quarterComparison,
        String trend,
        double improvement
) {}