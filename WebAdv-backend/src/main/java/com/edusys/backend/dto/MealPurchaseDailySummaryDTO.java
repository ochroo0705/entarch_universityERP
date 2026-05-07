package com.edusys.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MealPurchaseDailySummaryDTO(
        LocalDate purchaseDate,
        long purchaseCount,
        long quantity,
        BigDecimal totalAmount
) {}
