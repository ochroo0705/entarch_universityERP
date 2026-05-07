package com.edusys.backend.university.dto;

import com.edusys.backend.university.model.ServiceRequestStatus;
import java.time.LocalDateTime;

public record ServiceRequestResponse(
        Long id,
        String requestNumber,
        Long studentId,
        String studentName,
        String requestType,
        String description,
        ServiceRequestStatus status,
        String assignedOffice,
        Long assignedUserId,
        String assignedUserName,
        String holdReason,
        LocalDateTime dueAt,
        String slaStatus,
        Boolean attachmentRequired,
        Boolean attachmentSatisfied,
        LocalDateTime requestedAt,
        LocalDateTime updatedAt,
        LocalDateTime completedAt
) {}
