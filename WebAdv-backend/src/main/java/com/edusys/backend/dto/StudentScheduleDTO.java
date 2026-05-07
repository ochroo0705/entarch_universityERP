package com.edusys.backend.dto;

public record StudentScheduleDTO(
        int dayOfWeek,
        int periodNumber,
        String startTime,
        String endTime,
        String roomNumber,
        String subject,
        String teacher,
        String className
) {}
