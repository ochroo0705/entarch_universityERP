package com.edusys.backend.university.dto;

import java.math.BigDecimal;

public record BankPaymentCallbackResponse(
        Long invoiceId,
        String invoiceNumber,
        String invoiceStatus,
        BigDecimal paidAmount,
        String referenceNumber,
        UniversityIntegrationRunResponse integrationRun
) {}
