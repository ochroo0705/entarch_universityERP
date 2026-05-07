package com.edusys.backend.dto;

public record TeacherScheduleDTO(
        Long scheduleId,
        Integer dayOfWeek,
        Integer periodNumber,
        String startTime,
        String endTime,
        String roomNumber,
        String subject,
        String className,
        Integer gradeLevel
) {}
