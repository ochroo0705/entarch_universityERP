package com.edusys.backend.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record FacultyLeaveRequest(
        @NotNull Long facultyProfileId,
        @NotBlank String leaveType,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason
) {}
