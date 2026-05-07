package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityServiceType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityServiceTypeRepository extends JpaRepository<UniversityServiceType, Long> {
    List<UniversityServiceType> findAllByOrderByNameAscIdAsc();
    List<UniversityServiceType> findByActiveTrueOrderByNameAscIdAsc();
    Optional<UniversityServiceType> findByCodeIgnoreCase(String code);
    Optional<UniversityServiceType> findByNameIgnoreCase(String name);
}
