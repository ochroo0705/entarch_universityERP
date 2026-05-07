package com.edusys.backend.university.dto;

public record DepartmentResponse(
        Long id,
        String code,
        String name,
        Boolean active
) {}
