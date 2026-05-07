package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityReportRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityReportRunRepository extends JpaRepository<UniversityReportRun, Long> {
    List<UniversityReportRun> findTop20ByOrderByGeneratedAtDescIdDesc();
}
