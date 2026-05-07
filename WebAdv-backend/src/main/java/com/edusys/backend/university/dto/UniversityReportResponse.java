package com.edusys.backend.university.dto;

import java.math.BigDecimal;
import java.util.List;

public record UniversityReportResponse(
        long applicants,
        long acceptedApplicants,
        long convertedStudents,
        long selectedCourses,
        long billedSelections,
        long serviceRequests,
        long openServiceRequests,
        long heldServiceRequests,
        long prerequisiteRules,
        long academicRecords,
        long auditEvents,
        long financeInvoices,
        BigDecimal billedAmount,
        BigDecimal outstandingBalance,
        List<UniversityReportBreakdownResponse> admissionsByStatus,
        List<UniversityReportBreakdownResponse> serviceRequestsByStatus,
        List<UniversityReportBreakdownResponse> financeByStatus,
        List<UniversityReportBreakdownResponse> serviceQueues,
        List<UniversityReportBreakdownResponse> academicPolicyMetrics,
        List<UniversityReportBreakdownResponse> programRequirementProgress
) {}
