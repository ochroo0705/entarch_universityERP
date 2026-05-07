package com.edusys.backend.ai.repository;

import com.edusys.backend.ai.model.StudentRiskIndicatorSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRiskIndicatorSnapshotRepository extends JpaRepository<StudentRiskIndicatorSnapshot, Long> {
    List<StudentRiskIndicatorSnapshot> findByRiskSnapshot_IdOrderByIdAsc(Long riskSnapshotId);
}
