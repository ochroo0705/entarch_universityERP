package com.edusys.backend.university.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourseSelectionBatchResponse(
        Long studentId,
        String studentName,
        String academicYear,
        Integer semester,
        Integer totalCredits,
        Long invoiceId,
        String invoiceNumber,
        BigDecimal invoiceAmount,
        List<CourseSelectionResponse> selections
) {}
