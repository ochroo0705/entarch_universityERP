package com.edusys.backend.university.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record BankPaymentCallbackRequest(
        Long invoiceId,
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        BigDecimal amount,
        String referenceNumber
) {}
