package com.edusys.backend.ai.dto;

import java.math.BigDecimal;

public record RiskConfigResponse(
        String configKey,
        String configVersion,
        BigDecimal attendanceWeight,
        BigDecimal latenessWeight,
        BigDecimal homeworkWeight,
        BigDecimal gradeWeight,
        Integer lowMaxScore,
        Integer mediumMaxScore,
        Integer attendanceWindowDays,
        Integer homeworkWindowDays,
        Integer gradeWindowDays,
        Boolean isActive
) {
}
