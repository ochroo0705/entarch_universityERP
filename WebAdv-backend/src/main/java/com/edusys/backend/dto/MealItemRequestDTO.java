package com.edusys.backend.dto;

import com.edusys.backend.model.MealItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MealItemRequestDTO(
        @NotBlank String name,
        String description,
        @NotNull MealItem.MealType mealType,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        Boolean available
) {}
