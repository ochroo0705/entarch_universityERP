package com.edusys.backend.university.dto;

import com.edusys.backend.university.model.ServiceRequestStatus;
import java.math.BigDecimal;
import java.util.List;

public record GraduationClearanceResponse(
        Long requestId,
        String requestNumber,
        Long studentId,
        String studentName,
        String programName,
        ServiceRequestStatus status,
        Boolean eligible,
        BigDecimal outstandingBalance,
        Boolean attachmentRequired,
        Boolean attachmentSatisfied,
        Integer remainingCredits,
        List<String> missingRequirements,
        DegreeAuditResponse degreeAudit
) {}
