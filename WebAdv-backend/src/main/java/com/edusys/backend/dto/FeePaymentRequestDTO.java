package com.edusys.backend.dto;

import com.edusys.backend.model.FeePayment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeePaymentRequestDTO(
        @NotNull Long invoiceId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        LocalDate paymentDate,
        @NotNull FeePayment.Method method,
        FeePayment.Status status,
        String referenceNumber,
        String notes
) {}
