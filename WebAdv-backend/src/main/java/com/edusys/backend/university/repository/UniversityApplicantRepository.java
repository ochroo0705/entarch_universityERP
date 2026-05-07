package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.ApplicantStatus;
import com.edusys.backend.university.model.UniversityApplicant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UniversityApplicantRepository extends JpaRepository<UniversityApplicant, Long> {
    List<UniversityApplicant> findAllByOrderBySubmittedAtDescIdDesc();
    List<UniversityApplicant> findByStatusOrderBySubmittedAtDescIdDesc(ApplicantStatus status);
    Optional<UniversityApplicant> findByEmail(String email);
    long countByApplicationNumberStartingWith(String prefix);
    long countByStatus(ApplicantStatus status);
}
