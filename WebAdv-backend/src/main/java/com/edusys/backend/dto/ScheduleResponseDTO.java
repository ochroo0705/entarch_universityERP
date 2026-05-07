package com.edusys.backend.dto;

public record ScheduleResponseDTO(
        Long id,
        Long teachingAssignmentId,
        Integer dayOfWeek,
        Integer periodNumber,
        String startTime,
        String endTime,
        String roomNumber,
        String subject,
        String teacher,
        String className,
        Boolean isActive
) {}
