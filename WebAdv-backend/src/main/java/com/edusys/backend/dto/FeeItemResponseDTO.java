package com.edusys.backend.dto;

import com.edusys.backend.model.FeeItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FeeItemResponseDTO(
        Long id,
        String name,
        String description,
        FeeItem.Category category,
        BigDecimal amount,
        Boolean active,
        LocalDateTime createdAt
) {}
