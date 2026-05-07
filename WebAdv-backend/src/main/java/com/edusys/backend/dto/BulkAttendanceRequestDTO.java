package com.edusys.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record BulkAttendanceRequestDTO(
        Long teachingAssignmentId,
        LocalDate attendanceDate,
        Integer periodNumber,
        List<BulkAttendanceItem> attendances
) {}
