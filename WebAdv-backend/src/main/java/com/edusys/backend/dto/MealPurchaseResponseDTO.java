package com.edusys.backend.dto;

import com.edusys.backend.model.MealPurchase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MealPurchaseResponseDTO(
        Long id,
        Long studentId,
        String studentName,
        Long mealItemId,
        String mealItemName,
        Long mealPlanId,
        String mealPlanName,
        Integer quantity,
        BigDecimal totalAmount,
        LocalDate purchaseDate,
        MealPurchase.Status status,
        String notes,
        LocalDateTime createdAt
) {}
