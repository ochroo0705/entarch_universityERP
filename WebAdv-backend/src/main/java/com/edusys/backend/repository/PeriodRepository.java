package com.edusys.backend.repository;

import com.edusys.backend.model.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodRepository extends JpaRepository<Period, Integer> {
    // Optional: custom query methods can go here
}
