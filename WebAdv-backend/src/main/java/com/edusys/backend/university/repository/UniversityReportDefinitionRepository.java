package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityReportDefinition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityReportDefinitionRepository extends JpaRepository<UniversityReportDefinition, Long> {
    List<UniversityReportDefinition> findByActiveTrueOrderByCategoryAscNameAsc();
    Optional<UniversityReportDefinition> findByReportKeyAndActiveTrue(String reportKey);
}
