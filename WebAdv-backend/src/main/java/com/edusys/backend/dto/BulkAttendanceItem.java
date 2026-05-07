package com.edusys.backend.dto;

public record BulkAttendanceItem(
        Long studentId,
        AttendanceRequestDTO.AttendanceStatus status,
        String remarks
) {}
