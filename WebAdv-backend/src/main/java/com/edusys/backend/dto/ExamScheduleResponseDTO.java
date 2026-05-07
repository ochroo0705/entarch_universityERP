package com.edusys.backend.dto;

public record ExamScheduleResponseDTO(
        Long id,
        Long teachingAssignmentId,
        Long teacherId,
        Long classId,
        Long subjectId,
        String examDate,
        String startTime,
        String endTime,
        String roomNumber,
        String title,
        String notes,
        String subject,
        String teacher,
        String className,
        Integer grade,
        Boolean published,
        Boolean isActive,
        String createdAt,
        String updatedAt
) {
}
