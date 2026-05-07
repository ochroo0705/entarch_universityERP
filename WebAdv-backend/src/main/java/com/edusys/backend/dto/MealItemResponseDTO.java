package com.edusys.backend.dto;

import com.edusys.backend.model.MealItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MealItemResponseDTO(
        Long id,
        String name,
        String description,
        MealItem.MealType mealType,
        BigDecimal price,
        Boolean available,
        LocalDateTime createdAt
) {}
