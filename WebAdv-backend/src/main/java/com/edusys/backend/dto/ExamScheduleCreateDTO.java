package com.edusys.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamScheduleCreateDTO(
        @NotNull @Min(1) Long teachingAssignmentId,
        @NotNull LocalDate examDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @Size(max = 30) String roomNumber,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 5000) String notes,
        @NotNull Boolean published
) {
}
