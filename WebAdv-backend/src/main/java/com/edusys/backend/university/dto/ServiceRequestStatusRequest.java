package com.edusys.backend.university.dto;

import com.edusys.backend.university.model.ServiceRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceRequestStatusRequest(
        @NotNull ServiceRequestStatus status,
        @Size(max = 120) String assignedOffice,
        Long assignedUserId,
        @Size(max = 2000) String notes
) {}
