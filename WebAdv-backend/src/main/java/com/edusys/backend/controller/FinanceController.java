package com.edusys.backend.controller;

import com.edusys.backend.dto.*;
import com.edusys.backend.model.FeeInvoice;
import com.edusys.backend.service.FinanceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance")
@Tag(name = "Finance and Cafeteria", description = "APIs for fees, billing, payments, and cafeteria records")
@SecurityRequirement(name = "bearerAuth")
public class FinanceController {
    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/fee-items")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_STAFF')")
    public List<FeeItemResponseDTO> getFeeItems() {
        return financeService.getFeeItems();
    }

    @PostMapping("/fee-items")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_STAFF')")
    public FeeItemResponseDTO createFeeItem(@Valid @RequestBody FeeItemRequestDTO request) {
        return financeService.createFeeItem(request);
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_STAFF')")
    public List<FeeInvoiceResponseDTO> getInvoices(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) FeeInvoice.Status status) {
        return financeService.getInvoices(studentId, status);
    }

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_STAFF')")
    public FeeInvoiceResponseDTO createInvoice(@Valid @RequestBody FeeInvoiceRequestDTO request) {
        return financeService.createInvoice(request);
    }

    @PutMapping("/invoices/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_STAFF')")
    public FeeInvoiceResponseDTO updateInvoice(
            @PathVariable Long invoiceId,
            @Valid @RequestBody FeeInvoiceRequestDTO request) {
        return financeService.updateInvoice(invoiceId, request);
    }

    @PostMapping("/invoices/{invoiceId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_STAFF')")
    public FeeInvoiceResponseDTO cancelInvoice(
            @PathVariable Long invoiceId,
            @RequestBody(required = false) FeeInvoiceStatusRequestDTO request) {
        return financeService.cancelInvoice(invoiceId, request);
    }

    @PostMapping("/invoices/{invoiceId}/waive")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_STAFF')")
    public FeeInvoiceResponseDTO waiveInvoice(
            @PathVariable Long invoiceId,
            @RequestBody(required = false) FeeInvoiceStatusRequestDTO request) {
        return financeService.waiveInvoice(invoiceId, request);
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_STAFF')")
    public FeeInvoiceResponseDTO recordPayment(@Valid @RequestBody FeePaymentRequestDTO request) {
        return financeService.recordPayment(request);
    }

    @GetMapping("/meal-plans")
    @PreAuthorize("hasAnyRole('ADMIN','PARENT','STUDENT','CAFETERIA_STAFF')")
    public List<MealPlanResponseDTO> getMealPlans() {
        return financeService.getMealPlans();
    }

    @PostMapping("/meal-plans")
    @PreAuthorize("hasAnyRole('ADMIN','CAFETERIA_STAFF')")
    public MealPlanResponseDTO createMealPlan(@Valid @RequestBody MealPlanRequestDTO request) {
        return financeService.createMealPlan(request);
    }

    @GetMapping("/meal-items")
    @PreAuthorize("hasAnyRole('ADMIN','PARENT','STUDENT','CAFETERIA_STAFF')")
    public List<MealItemResponseDTO> getMealItems() {
        return financeService.getMealItems();
    }

    @PostMapping("/meal-items")
    @PreAuthorize("hasAnyRole('ADMIN','CAFETERIA_STAFF')")
    public MealItemResponseDTO createMealItem(@Valid @RequestBody MealItemRequestDTO request) {
        return financeService.createMealItem(request);
    }

    @GetMapping("/meal-purchases")
    @PreAuthorize("hasAnyRole('ADMIN','CAFETERIA_STAFF')")
    public List<MealPurchaseResponseDTO> getMealPurchases(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return financeService.getMealPurchases(studentId, startDate, endDate);
    }

    @GetMapping("/meal-purchases/daily-summary")
    @PreAuthorize("hasAnyRole('ADMIN','CAFETERIA_STAFF')")
    public List<MealPurchaseDailySummaryDTO> getMealPurchaseDailySummary(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return financeService.getMealPurchaseDailySummary(studentId, startDate, endDate);
    }

    @PostMapping("/meal-purchases")
    @PreAuthorize("hasAnyRole('ADMIN','CAFETERIA_STAFF')")
    public MealPurchaseResponseDTO recordMealPurchase(@Valid @RequestBody MealPurchaseRequestDTO request) {
        return financeService.recordMealPurchase(request);
    }

    @GetMapping("/students/{studentId}/summary")
    @PreAuthorize("hasRole('FINANCE_STAFF') or (hasAnyRole('STUDENT','PARENT','ADMIN') and @studentAccess.canAccessStudent(#studentId))")
    public FinanceSummaryDTO getStudentSummary(@PathVariable Long studentId) {
        return financeService.getStudentSummary(studentId);
    }
}
