package com.edusys.backend.dto;

import com.edusys.backend.model.MealPurchase;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MealPurchaseRequestDTO(
        @NotNull Long studentId,
        @NotNull Long mealItemId,
        Long mealPlanId,
        @NotNull @Min(1) Integer quantity,
        LocalDate purchaseDate,
        MealPurchase.Status status,
        String notes
) {}
