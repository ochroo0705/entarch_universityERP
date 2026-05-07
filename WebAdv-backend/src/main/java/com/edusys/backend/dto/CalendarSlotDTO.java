package com.edusys.backend.dto;

public record CalendarSlotDTO(
        Long scheduleId,
        Integer periodNumber,
        String startTime,
        String endTime,
        String roomNumber,
        String subject,
        String className,
        String teacher,
        Integer gradeLevel
) {}
