package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.AcademicRecordStatus;
import com.edusys.backend.university.model.UniversityAcademicRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityAcademicRecordRepository extends JpaRepository<UniversityAcademicRecord, Long> {
    List<UniversityAcademicRecord> findByStudent_IdOrderByCompletedAtDescIdDesc(Long studentId);
    Optional<UniversityAcademicRecord> findByStudent_IdAndSubject_Id(Long studentId, Long subjectId);
    boolean existsByStudent_IdAndSubject_IdAndStatus(Long studentId, Long subjectId, AcademicRecordStatus status);
}
