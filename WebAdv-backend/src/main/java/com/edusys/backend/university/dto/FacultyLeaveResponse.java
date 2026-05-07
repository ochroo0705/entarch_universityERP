package com.edusys.backend.university.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FacultyLeaveResponse(
        Long id,
        Long facultyProfileId,
        String facultyName,
        String department,
        String leaveType,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String reason,
        String decisionNotes,
        LocalDateTime requestedAt,
        LocalDateTime decidedAt
) {}
