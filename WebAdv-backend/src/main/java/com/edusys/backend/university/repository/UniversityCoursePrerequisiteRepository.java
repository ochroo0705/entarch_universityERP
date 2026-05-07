package com.edusys.backend.university.repository;

import com.edusys.backend.university.model.UniversityCoursePrerequisite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityCoursePrerequisiteRepository extends JpaRepository<UniversityCoursePrerequisite, Long> {
    List<UniversityCoursePrerequisite> findBySubject_IdOrderByPrerequisiteSubject_SubjectCodeAsc(Long subjectId);
    Optional<UniversityCoursePrerequisite> findBySubject_IdAndPrerequisiteSubject_Id(Long subjectId, Long prerequisiteSubjectId);
}
