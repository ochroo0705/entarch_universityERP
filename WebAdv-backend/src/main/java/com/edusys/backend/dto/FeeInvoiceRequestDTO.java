package com.edusys.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FeeInvoiceRequestDTO(
        @NotNull Long studentId,
        LocalDate dueDate,
        String notes,
        @NotEmpty List<@Valid Line> lines
) {
    public record Line(
            Long feeItemId,
            String description,
            @NotNull @DecimalMin("0.00") BigDecimal amount
    ) {}
}
