package com.edusys.backend.dto;

import java.time.LocalDateTime;

public record ClassResponseDTO(
        Long id,
        String className,
        Integer grade,
        String section,
        Long homeroomTeacherId,
        String homeroomTeacherUsername,
        String roomNumber,
        String academicYear,
        Integer studentCount,
        Boolean isActive,
        LocalDateTime createdAt
) {}
