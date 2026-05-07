package com.edusys.backend.university.dto;

import java.math.BigDecimal;

public record UniversityReportBreakdownResponse(
        String label,
        long count,
        BigDecimal amount
) {}
