package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityErpEventLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityErpEventLogRepository extends JpaRepository<UniversityErpEventLog, Long> {
    List<UniversityErpEventLog> findTop20ByOrderByCreatedAtDescIdDesc();
}
