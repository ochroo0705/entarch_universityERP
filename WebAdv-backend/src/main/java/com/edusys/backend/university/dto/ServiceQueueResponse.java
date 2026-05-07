package com.edusys.backend.university.dto;

public record ServiceQueueResponse(
        String office,
        long openRequests,
        long unassignedRequests,
        long dueSoonRequests,
        long overdueRequests
) {}
