package com.edusys.backend.repository;

import com.edusys.backend.model.FeeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeItemRepository extends JpaRepository<FeeItem, Long> {
    List<FeeItem> findAllByOrderByCreatedAtDescIdDesc();
}
