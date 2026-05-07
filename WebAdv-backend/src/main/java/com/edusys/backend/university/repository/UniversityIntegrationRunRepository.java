package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityIntegrationRun;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityIntegrationRunRepository extends JpaRepository<UniversityIntegrationRun, Long> {
    List<UniversityIntegrationRun> findTop20ByOrderByExchangedAtDescIdDesc();
    Optional<UniversityIntegrationRun> findTop1ByIntegrationKeyIgnoreCaseOrderByExchangedAtDescIdDesc(String integrationKey);
}
