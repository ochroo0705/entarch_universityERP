package com.edusys.backend.repository;

import com.edusys.backend.model.MealItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealItemRepository extends JpaRepository<MealItem, Long> {
    List<MealItem> findAllByOrderByCreatedAtDescIdDesc();
}
