package com.edusys.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinanceSummaryDTO(
        Long studentId,
        String studentName,
        BigDecimal billedAmount,
        BigDecimal paidAmount,
        BigDecimal balance,
        BigDecimal cafeteriaSpend,
        List<FeeInvoiceResponseDTO> invoices,
        List<MealPurchaseResponseDTO> mealPurchases
) {}
