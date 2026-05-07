package com.edusys.backend.dto;

import com.edusys.backend.model.FeeItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FeeItemRequestDTO(
        @NotBlank String name,
        String description,
        @NotNull FeeItem.Category category,
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        Boolean active
) {}
