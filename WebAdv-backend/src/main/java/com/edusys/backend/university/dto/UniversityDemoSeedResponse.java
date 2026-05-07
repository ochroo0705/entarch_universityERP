package com.edusys.backend.university.dto;

import java.util.List;

public record UniversityDemoSeedResponse(
        Long studentId,
        String studentName,
        List<Long> courseIds,
        Long prerequisiteRuleId,
        Long academicRecordId,
        Long serviceRequestId,
        String message
) {}
