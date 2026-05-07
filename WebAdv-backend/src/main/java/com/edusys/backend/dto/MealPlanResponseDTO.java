package com.edusys.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MealPlanResponseDTO(
        Long id,
        String name,
        String description,
        BigDecimal pricePerMeal,
        Boolean active,
        LocalDateTime createdAt
) {}
