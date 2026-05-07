package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityIntegrationConnection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityIntegrationConnectionRepository extends JpaRepository<UniversityIntegrationConnection, Long> {
    List<UniversityIntegrationConnection> findAllByOrderByIntegrationKeyAsc();
    Optional<UniversityIntegrationConnection> findByIntegrationKeyIgnoreCase(String integrationKey);
}
