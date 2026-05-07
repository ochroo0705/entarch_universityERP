package com.edusys.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "fee_invoice_lines")
public class FeeInvoiceLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private FeeInvoice invoice;

    @ManyToOne
    @JoinColumn(name = "fee_item_id")
    private FeeItem feeItem;

    private String description;
    private BigDecimal amount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FeeInvoice getInvoice() { return invoice; }
    public void setInvoice(FeeInvoice invoice) { this.invoice = invoice; }
    public FeeItem getFeeItem() { return feeItem; }
    public void setFeeItem(FeeItem feeItem) { this.feeItem = feeItem; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
