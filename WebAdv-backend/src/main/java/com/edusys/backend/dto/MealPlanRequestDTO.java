package com.edusys.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MealPlanRequestDTO(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin("0.00") BigDecimal pricePerMeal,
        Boolean active
) {}
