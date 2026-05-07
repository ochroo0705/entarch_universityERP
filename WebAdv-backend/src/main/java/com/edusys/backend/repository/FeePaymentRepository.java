package com.edusys.backend.repository;

import com.edusys.backend.model.FeePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {
    List<FeePayment> findByStudent_IdOrderByPaymentDateDescIdDesc(Long studentId);
    List<FeePayment> findByInvoice_IdOrderByPaymentDateDescIdDesc(Long invoiceId);
}
