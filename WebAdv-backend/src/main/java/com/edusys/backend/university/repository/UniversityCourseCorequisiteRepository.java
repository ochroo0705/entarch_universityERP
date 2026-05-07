package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityCourseCorequisite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityCourseCorequisiteRepository extends JpaRepository<UniversityCourseCorequisite, Long> {
    List<UniversityCourseCorequisite> findBySubject_IdOrderByCorequisiteSubject_SubjectCodeAsc(Long subjectId);
    Optional<UniversityCourseCorequisite> findBySubject_IdAndCorequisiteSubject_Id(Long subjectId, Long corequisiteSubjectId);
}
