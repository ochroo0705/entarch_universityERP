package com.edusys.backend.university.dto;

import jakarta.validation.constraints.Size;

public record ServiceRequestAssignmentRequest(
        @Size(max = 120) String assignedOffice,
        Long assignedUserId,
        @Size(max = 2000) String notes
) {}
