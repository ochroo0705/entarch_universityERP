package com.edusys.backend.dto;

import com.edusys.backend.model.FeeInvoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FeeInvoiceResponseDTO(
        Long id,
        Long studentId,
        String studentName,
        String invoiceNumber,
        LocalDate dueDate,
        FeeInvoice.Status status,
        String notes,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balance,
        LocalDateTime createdAt,
        List<Line> lines,
        List<Payment> payments
) {
    public record Line(Long id, Long feeItemId, String description, BigDecimal amount) {}
    public record Payment(Long id, BigDecimal amount, LocalDate paymentDate, String method, String status, String referenceNumber) {}
}
