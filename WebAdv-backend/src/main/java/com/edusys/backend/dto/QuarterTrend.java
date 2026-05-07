package com.edusys.backend.dto;

public record QuarterTrend(
        int quarter,
        double gpa,
        String performance
) {}