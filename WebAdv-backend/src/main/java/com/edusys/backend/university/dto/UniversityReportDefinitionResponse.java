package com.edusys.backend.university.dto;

import java.util.List;

public record UniversityReportDefinitionResponse(
        Long id,
        String reportKey,
        String name,
        String category,
        String description,
        List<String> visibleToRoles
) {}
