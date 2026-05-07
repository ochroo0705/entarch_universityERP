package com.edusys.backend.dto;

public record ChildSummaryDTO(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email
) {}
