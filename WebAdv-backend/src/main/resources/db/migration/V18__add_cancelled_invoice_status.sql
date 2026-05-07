ALTER TABLE fee_invoices
    DROP CONSTRAINT IF EXISTS fee_invoices_status_check;

ALTER TABLE fee_invoices
    ADD CONSTRAINT fee_invoices_status_check
    CHECK (status IN ('DRAFT','ISSUED','PARTIALLY_PAID','PAID','WAIVED','OVERDUE','CANCELLED'));
