package com.edusys.backend.dto;

public record TeacherSummaryDTO(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email
) {}
