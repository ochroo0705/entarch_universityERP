package com.edusys.backend.dto;

public record SchoolStatsDTO(
        long teacherCount,
        long studentCount,
        long classCount,
        long teachingAssignmentCount
) {}
