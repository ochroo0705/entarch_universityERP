package com.edusys.backend.repository;

import com.edusys.backend.model.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    List<MealPlan> findAllByOrderByCreatedAtDescIdDesc();
}
