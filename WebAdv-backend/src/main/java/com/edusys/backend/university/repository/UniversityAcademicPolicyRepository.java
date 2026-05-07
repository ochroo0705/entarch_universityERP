package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityAcademicPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityAcademicPolicyRepository extends JpaRepository<UniversityAcademicPolicy, Long> {
    Optional<UniversityAcademicPolicy> findFirstByActiveTrueOrderByUpdatedAtDescIdDesc();
}
