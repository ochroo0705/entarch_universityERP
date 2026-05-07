package com.edusys.backend.ai.service;

public record HomeworkWindowMetrics(
        long assignedHomeworkCount,
        long missingHomeworkCount
) {
}
