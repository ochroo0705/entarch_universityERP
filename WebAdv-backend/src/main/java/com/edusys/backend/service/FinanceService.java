package com.edusys.backend.service;

import com.edusys.backend.dto.*;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.*;
import com.edusys.backend.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class FinanceService {
    private final FeeItemRepository feeItemRepository;
    private final FeeInvoiceRepository feeInvoiceRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final MealPlanRepository mealPlanRepository;
    private final MealItemRepository mealItemRepository;
    private final MealPurchaseRepository mealPurchaseRepository;
    private final UserRepository userRepository;

    public FinanceService(
            FeeItemRepository feeItemRepository,
            FeeInvoiceRepository feeInvoiceRepository,
            FeePaymentRepository feePaymentRepository,
            MealPlanRepository mealPlanRepository,
            MealItemRepository mealItemRepository,
            MealPurchaseRepository mealPurchaseRepository,
            UserRepository userRepository) {
        this.feeItemRepository = feeItemRepository;
        this.feeInvoiceRepository = feeInvoiceRepository;
        this.feePaymentRepository = feePaymentRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.mealItemRepository = mealItemRepository;
        this.mealPurchaseRepository = mealPurchaseRepository;
        this.userRepository = userRepository;
    }

    public List<FeeItemResponseDTO> getFeeItems() {
        return feeItemRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(this::mapFeeItem)
                .toList();
    }

    @Transactional
    public FeeItemResponseDTO createFeeItem(FeeItemRequestDTO request) {
        FeeItem item = new FeeItem();
        item.setName(request.name());
        item.setDescription(request.description());
        item.setCategory(request.category());
        item.setAmount(request.amount());
        item.setActive(request.active() == null || request.active());
        item.setCreatedAt(LocalDateTime.now());
        return mapFeeItem(feeItemRepository.save(item));
    }

    public List<FeeInvoiceResponseDTO> getInvoices(Long studentId, FeeInvoice.Status status) {
        List<FeeInvoice> invoices;
        if (studentId != null && status != null) {
            invoices = feeInvoiceRepository.findByStudent_IdAndStatusOrderByCreatedAtDescIdDesc(studentId, status);
        } else if (studentId != null) {
            invoices = feeInvoiceRepository.findByStudent_IdOrderByCreatedAtDescIdDesc(studentId);
        } else if (status != null) {
            invoices = feeInvoiceRepository.findByStatusOrderByCreatedAtDescIdDesc(status);
        } else {
            invoices = feeInvoiceRepository.findAllByOrderByCreatedAtDescIdDesc();
        }
        return invoices.stream().map(this::mapInvoice).toList();
    }

    @Transactional
    public FeeInvoiceResponseDTO createInvoice(FeeInvoiceRequestDTO request) {
        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        FeeInvoice invoice = new FeeInvoice();
        invoice.setStudent(student);
        invoice.setInvoiceNumber(nextInvoiceNumber());
        invoice.setDueDate(request.dueDate());
        invoice.setNotes(request.notes());
        invoice.setStatus(FeeInvoice.Status.ISSUED);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());

        addInvoiceLines(invoice, request.lines());

        return mapInvoice(feeInvoiceRepository.save(invoice));
    }

    @Transactional
    public FeeInvoiceResponseDTO updateInvoice(Long invoiceId, FeeInvoiceRequestDTO request) {
        FeeInvoice invoice = feeInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        if (invoice.getStatus() == FeeInvoice.Status.CANCELLED || invoice.getStatus() == FeeInvoice.Status.WAIVED || invoice.getStatus() == FeeInvoice.Status.PAID) {
            throw new IllegalArgumentException("Closed invoices cannot be edited");
        }
        if (paidAmount(invoice).compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("Invoices with completed payments cannot be edited");
        }

        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        invoice.setStudent(student);
        invoice.setDueDate(request.dueDate());
        invoice.setNotes(request.notes());
        invoice.setUpdatedAt(LocalDateTime.now());
        if (invoice.getStatus() != FeeInvoice.Status.DRAFT) {
            invoice.setStatus(FeeInvoice.Status.ISSUED);
        }

        invoice.getLines().clear();
        addInvoiceLines(invoice, request.lines());

        return mapInvoice(feeInvoiceRepository.save(invoice));
    }

    private void addInvoiceLines(FeeInvoice invoice, List<FeeInvoiceRequestDTO.Line> lines) {
        for (FeeInvoiceRequestDTO.Line requestLine : lines) {
            FeeInvoiceLine line = new FeeInvoiceLine();
            line.setInvoice(invoice);
            if (requestLine.feeItemId() != null) {
                FeeItem item = feeItemRepository.findById(requestLine.feeItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Fee item not found"));
                line.setFeeItem(item);
                line.setDescription(requestLine.description() == null || requestLine.description().isBlank()
                        ? item.getName()
                        : requestLine.description());
            } else {
                line.setDescription(requestLine.description());
            }
            line.setAmount(requestLine.amount());
            invoice.getLines().add(line);
        }
    }

    @Transactional
    public FeeInvoiceResponseDTO recordPayment(FeePaymentRequestDTO request) {
        FeeInvoice invoice = feeInvoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        if (invoice.getStatus() == FeeInvoice.Status.CANCELLED || invoice.getStatus() == FeeInvoice.Status.WAIVED) {
            throw new IllegalArgumentException("Payments cannot be recorded against cancelled or waived invoices");
        }

        FeePayment payment = new FeePayment();
        payment.setInvoice(invoice);
        payment.setStudent(invoice.getStudent());
        payment.setAmount(request.amount());
        payment.setPaymentDate(request.paymentDate() == null ? LocalDate.now() : request.paymentDate());
        payment.setMethod(request.method());
        payment.setStatus(request.status() == null ? FeePayment.Status.COMPLETED : request.status());
        payment.setReferenceNumber(request.referenceNumber());
        payment.setNotes(request.notes());
        payment.setRecordedBy(getCurrentUser());
        payment.setCreatedAt(LocalDateTime.now());
        feePaymentRepository.save(payment);

        updateInvoiceStatus(invoice);
        return mapInvoice(feeInvoiceRepository.save(invoice));
    }

    @Transactional
    public FeeInvoiceResponseDTO cancelInvoice(Long invoiceId, FeeInvoiceStatusRequestDTO request) {
        FeeInvoice invoice = feeInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        BigDecimal paid = paidAmount(invoice);

        if (invoice.getStatus() == FeeInvoice.Status.CANCELLED) {
            return mapInvoice(invoice);
        }
        if (invoice.getStatus() == FeeInvoice.Status.WAIVED) {
            throw new IllegalArgumentException("Waived invoices cannot be cancelled");
        }
        if (paid.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("Invoices with completed payments cannot be cancelled");
        }

        invoice.setStatus(FeeInvoice.Status.CANCELLED);
        invoice.setNotes(appendActionNote(invoice.getNotes(), "Cancelled", request == null ? null : request.notes()));
        invoice.setUpdatedAt(LocalDateTime.now());
        return mapInvoice(feeInvoiceRepository.save(invoice));
    }

    @Transactional
    public FeeInvoiceResponseDTO waiveInvoice(Long invoiceId, FeeInvoiceStatusRequestDTO request) {
        FeeInvoice invoice = feeInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (invoice.getStatus() == FeeInvoice.Status.WAIVED) {
            return mapInvoice(invoice);
        }
        if (invoice.getStatus() == FeeInvoice.Status.CANCELLED) {
            throw new IllegalArgumentException("Cancelled invoices cannot be waived");
        }
        if (balanceAmount(invoice).compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Only invoices with an outstanding balance can be waived");
        }

        invoice.setStatus(FeeInvoice.Status.WAIVED);
        invoice.setNotes(appendActionNote(invoice.getNotes(), "Waived", request == null ? null : request.notes()));
        invoice.setUpdatedAt(LocalDateTime.now());
        return mapInvoice(feeInvoiceRepository.save(invoice));
    }

    public List<MealPlanResponseDTO> getMealPlans() {
        return mealPlanRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(this::mapMealPlan)
                .toList();
    }

    @Transactional
    public MealPlanResponseDTO createMealPlan(MealPlanRequestDTO request) {
        MealPlan plan = new MealPlan();
        plan.setName(request.name());
        plan.setDescription(request.description());
        plan.setPricePerMeal(request.pricePerMeal());
        plan.setActive(request.active() == null || request.active());
        plan.setCreatedAt(LocalDateTime.now());
        return mapMealPlan(mealPlanRepository.save(plan));
    }

    public List<MealItemResponseDTO> getMealItems() {
        return mealItemRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(this::mapMealItem)
                .toList();
    }

    @Transactional
    public MealItemResponseDTO createMealItem(MealItemRequestDTO request) {
        MealItem item = new MealItem();
        item.setName(request.name());
        item.setDescription(request.description());
        item.setMealType(request.mealType());
        item.setPrice(request.price());
        item.setAvailable(request.available() == null || request.available());
        item.setCreatedAt(LocalDateTime.now());
        return mapMealItem(mealItemRepository.save(item));
    }

    public List<MealPurchaseResponseDTO> getMealPurchases(Long studentId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        List<MealPurchase> purchases = mealPurchaseRepository.findForFilters(studentId, startDate, endDate);
        return purchases.stream().map(this::mapMealPurchase).toList();
    }

    public List<MealPurchaseDailySummaryDTO> getMealPurchaseDailySummary(Long studentId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        Map<LocalDate, List<MealPurchase>> purchasesByDate = mealPurchaseRepository.findForFilters(studentId, startDate, endDate).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        MealPurchase::getPurchaseDate,
                        () -> new TreeMap<LocalDate, List<MealPurchase>>(Comparator.reverseOrder()),
                        java.util.stream.Collectors.toList()));

        return purchasesByDate.entrySet().stream()
                .map(entry -> new MealPurchaseDailySummaryDTO(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToLong(MealPurchase::getQuantity).sum(),
                        entry.getValue().stream().map(MealPurchase::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .toList();
    }

    @Transactional
    public MealPurchaseResponseDTO recordMealPurchase(MealPurchaseRequestDTO request) {
        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        MealItem item = mealItemRepository.findById(request.mealItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Meal item not found"));

        MealPlan plan = null;
        if (request.mealPlanId() != null) {
            plan = mealPlanRepository.findById(request.mealPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found"));
        }

        BigDecimal unitPrice = plan == null ? item.getPrice() : plan.getPricePerMeal();
        MealPurchase purchase = new MealPurchase();
        purchase.setStudent(student);
        purchase.setMealItem(item);
        purchase.setMealPlan(plan);
        purchase.setQuantity(request.quantity());
        purchase.setTotalAmount(unitPrice.multiply(BigDecimal.valueOf(request.quantity())));
        purchase.setPurchaseDate(request.purchaseDate() == null ? LocalDate.now() : request.purchaseDate());
        purchase.setStatus(request.status() == null ? MealPurchase.Status.SERVED : request.status());
        purchase.setNotes(request.notes());
        purchase.setRecordedBy(getCurrentUser());
        purchase.setCreatedAt(LocalDateTime.now());
        return mapMealPurchase(mealPurchaseRepository.save(purchase));
    }

    public FinanceSummaryDTO getStudentSummary(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        List<FeeInvoiceResponseDTO> invoices = getInvoices(studentId, null);
        List<MealPurchaseResponseDTO> purchases = getMealPurchases(studentId, null, null);

        BigDecimal billed = invoices.stream().map(FeeInvoiceResponseDTO::totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = invoices.stream().map(FeeInvoiceResponseDTO::paidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = invoices.stream().map(FeeInvoiceResponseDTO::balance).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cafeteriaSpend = purchases.stream().map(MealPurchaseResponseDTO::totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FinanceSummaryDTO(
                studentId,
                student.getFullName().trim(),
                billed,
                paid,
                balance,
                cafeteriaSpend,
                invoices,
                purchases
        );
    }

    private void updateInvoiceStatus(FeeInvoice invoice) {
        BigDecimal total = totalAmount(invoice);
        BigDecimal paid = paidAmount(invoice);
        if (paid.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(FeeInvoice.Status.ISSUED);
        } else if (paid.compareTo(total) >= 0) {
            invoice.setStatus(FeeInvoice.Status.PAID);
        } else {
            invoice.setStatus(FeeInvoice.Status.PARTIALLY_PAID);
        }
        invoice.setUpdatedAt(LocalDateTime.now());
    }

    private String nextInvoiceNumber() {
        String prefix = "INV-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        long next = feeInvoiceRepository.countByInvoiceNumberStartingWith(prefix) + 1;
        return prefix + String.format("%04d", next);
    }

    private BigDecimal totalAmount(FeeInvoice invoice) {
        return invoice.getLines().stream()
                .map(FeeInvoiceLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal paidAmount(FeeInvoice invoice) {
        List<FeePayment> payments = feePaymentRepository.findByInvoice_IdOrderByPaymentDateDescIdDesc(invoice.getId());
        return payments.stream()
                .filter(payment -> payment.getStatus() == FeePayment.Status.COMPLETED)
                .map(FeePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal balanceAmount(FeeInvoice invoice) {
        if (invoice.getStatus() == FeeInvoice.Status.WAIVED || invoice.getStatus() == FeeInvoice.Status.CANCELLED) {
            return BigDecimal.ZERO;
        }
        return totalAmount(invoice).subtract(paidAmount(invoice));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be on or before end date");
        }
    }

    private String appendActionNote(String existingNotes, String action, String actionNotes) {
        if (actionNotes == null || actionNotes.isBlank()) {
            return existingNotes;
        }
        String note = action + ": " + actionNotes.trim();
        if (existingNotes == null || existingNotes.isBlank()) {
            return note;
        }
        return existingNotes + "\n" + note;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    private FeeItemResponseDTO mapFeeItem(FeeItem item) {
        return new FeeItemResponseDTO(item.getId(), item.getName(), item.getDescription(), item.getCategory(), item.getAmount(), item.getActive(), item.getCreatedAt());
    }

    private FeeInvoiceResponseDTO mapInvoice(FeeInvoice invoice) {
        BigDecimal total = totalAmount(invoice);
        BigDecimal paid = invoice.getId() == null ? BigDecimal.ZERO : paidAmount(invoice);
        BigDecimal balance = invoice.getId() == null ? total.subtract(paid) : balanceAmount(invoice);
        List<FeeInvoiceResponseDTO.Line> lines = invoice.getLines().stream()
                .map(line -> new FeeInvoiceResponseDTO.Line(
                        line.getId(),
                        line.getFeeItem() == null ? null : line.getFeeItem().getId(),
                        line.getDescription(),
                        line.getAmount()))
                .toList();
        List<FeeInvoiceResponseDTO.Payment> payments = invoice.getId() == null ? List.of() : feePaymentRepository.findByInvoice_IdOrderByPaymentDateDescIdDesc(invoice.getId()).stream()
                .map(payment -> new FeeInvoiceResponseDTO.Payment(
                        payment.getId(),
                        payment.getAmount(),
                        payment.getPaymentDate(),
                        payment.getMethod().name(),
                        payment.getStatus().name(),
                        payment.getReferenceNumber()))
                .toList();

        return new FeeInvoiceResponseDTO(
                invoice.getId(),
                invoice.getStudent().getId(),
                invoice.getStudent().getFullName().trim(),
                invoice.getInvoiceNumber(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.getNotes(),
                total,
                paid,
                balance,
                invoice.getCreatedAt(),
                lines,
                payments
        );
    }

    private MealPlanResponseDTO mapMealPlan(MealPlan plan) {
        return new MealPlanResponseDTO(plan.getId(), plan.getName(), plan.getDescription(), plan.getPricePerMeal(), plan.getActive(), plan.getCreatedAt());
    }

    private MealItemResponseDTO mapMealItem(MealItem item) {
        return new MealItemResponseDTO(item.getId(), item.getName(), item.getDescription(), item.getMealType(), item.getPrice(), item.getAvailable(), item.getCreatedAt());
    }

    private MealPurchaseResponseDTO mapMealPurchase(MealPurchase purchase) {
        return new MealPurchaseResponseDTO(
                purchase.getId(),
                purchase.getStudent().getId(),
                purchase.getStudent().getFullName().trim(),
                purchase.getMealItem().getId(),
                purchase.getMealItem().getName(),
                purchase.getMealPlan() == null ? null : purchase.getMealPlan().getId(),
                purchase.getMealPlan() == null ? null : purchase.getMealPlan().getName(),
                purchase.getQuantity(),
                purchase.getTotalAmount(),
                purchase.getPurchaseDate(),
                purchase.getStatus(),
                purchase.getNotes(),
                purchase.getCreatedAt()
        );
    }
}
