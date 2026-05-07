package com.edusys.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ScheduleCreateDTO(
        @NotNull(message = "Teaching assignment ID is required")
        Long teachingAssignmentId,
        
        @NotNull(message = "Day of week is required")
        @Min(value = 1, message = "Day of week must be between 1 and 7")
        @Max(value = 7, message = "Day of week must be between 1 and 7")
        Integer dayOfWeek,
        
        @NotNull(message = "Period number is required")
        @Min(value = 1, message = "Period number must be at least 1")
        Integer periodNumber,
        
        @NotNull(message = "Start time is required")
        String startTime,
        
        @NotNull(message = "End time is required")
        String endTime,
        
        String roomNumber
) {}
