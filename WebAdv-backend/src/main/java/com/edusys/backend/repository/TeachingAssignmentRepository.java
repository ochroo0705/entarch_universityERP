package com.edusys.backend.repository;

import com.edusys.backend.model.TeachingAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long>, JpaSpecificationExecutor<TeachingAssignment> {

        @Override
        @EntityGraph(attributePaths = {
                        "teacher",
                        "subject",
                        "classEntity"
        })
        Page<TeachingAssignment> findAll(Specification<TeachingAssignment> spec, Pageable pageable);

        @EntityGraph(attributePaths = {
                        "teacher",
                        "subject",
                        "classEntity",
                        "classEntity.homeroomTeacher",
                        "classEntity.assistantTeachers"
        })
        Optional<TeachingAssignment> findDetailedById(Long id);

        @EntityGraph(attributePaths = {
                        "teacher",
                        "subject",
                        "classEntity",
                        "classEntity.homeroomTeacher",
                        "classEntity.assistantTeachers"
        })
        List<TeachingAssignment> findAllDetailedByTeacher_IdAndSubject_IdAndAcademicYearAndSemesterAndIsActiveTrue(
                Long teacherId,
                Long subjectId,
                String academicYear,
                Integer semester
        );

        @EntityGraph(attributePaths = {
                        "teacher",
                        "subject",
                        "classEntity",
                        "classEntity.homeroomTeacher",
                        "classEntity.assistantTeachers"
        })
        Optional<TeachingAssignment> findDetailedByTeacher_IdAndSubject_IdAndAcademicYearAndSemesterAndIsActiveTrueAndClassEntity_Id(
                Long teacherId,
                Long subjectId,
                String academicYear,
                Integer semester,
                Long classId
        );

        @EntityGraph(attributePaths = {
                        "teacher",
                        "subject",
                        "classEntity",
                        "classEntity.homeroomTeacher",
                        "classEntity.assistantTeachers"
        })
        List<TeachingAssignment> findAllDetailedByTeacher_IdAndIsActiveTrue(Long teacherId);

        boolean existsByTeacher_IdAndClassEntity_IdAndIsActiveTrue(Long teacherId, Long classId);

    boolean existsByTeacher_IdAndSubject_IdAndClassEntity_IdAndAcademicYearAndSemesterAndIsActiveTrue(
            Long teacherId,
            Long subjectId,
            Long classId,
            String academicYear,
            Integer semester
    );

    boolean existsByTeacher_IdAndSubject_IdAndClassEntity_IdAndAcademicYearAndSemesterAndIsActiveTrueAndIdNot(
            Long teacherId,
            Long subjectId,
            Long classId,
            String academicYear,
            Integer semester,
            Long id
    );

    @EntityGraph(attributePaths = {"teacher", "subject", "classEntity"})
    List<TeachingAssignment> findAllByIsActiveTrue();
}
