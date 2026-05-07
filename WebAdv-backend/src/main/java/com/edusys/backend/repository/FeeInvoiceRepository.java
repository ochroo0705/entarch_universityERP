package com.edusys.backend.repository;

import com.edusys.backend.model.FeeInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeInvoiceRepository extends JpaRepository<FeeInvoice, Long> {
    List<FeeInvoice> findAllByOrderByCreatedAtDescIdDesc();
    List<FeeInvoice> findByStudent_IdOrderByCreatedAtDescIdDesc(Long studentId);
    List<FeeInvoice> findByStatusOrderByCreatedAtDescIdDesc(FeeInvoice.Status status);
    List<FeeInvoice> findByStudent_IdAndStatusOrderByCreatedAtDescIdDesc(Long studentId, FeeInvoice.Status status);
    long countByInvoiceNumberStartingWith(String prefix);
}
