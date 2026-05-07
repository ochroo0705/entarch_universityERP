package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityDepartment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityDepartmentRepository extends JpaRepository<UniversityDepartment, Long> {
    List<UniversityDepartment> findAllByOrderByNameAscIdAsc();
    Optional<UniversityDepartment> findByCodeIgnoreCase(String code);
    Optional<UniversityDepartment> findByNameIgnoreCase(String name);
}
