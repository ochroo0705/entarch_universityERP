package com.edusys.backend.repository;

import com.edusys.backend.model.MealPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MealPurchaseRepository extends JpaRepository<MealPurchase, Long> {
    List<MealPurchase> findByStudent_IdOrderByPurchaseDateDescIdDesc(Long studentId);

    @Query("""
            select purchase from MealPurchase purchase
            where (:studentId is null or purchase.student.id = :studentId)
              and (:startDate is null or purchase.purchaseDate >= :startDate)
              and (:endDate is null or purchase.purchaseDate <= :endDate)
            order by purchase.purchaseDate desc, purchase.id desc
            """)
    List<MealPurchase> findForFilters(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
