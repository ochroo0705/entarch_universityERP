package com.edusys.backend.repository;

import com.edusys.backend.dto.UserClassSummaryDTO;
import com.edusys.backend.model.StudentEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Long>, JpaSpecificationExecutor<StudentEnrollment> {
    @Override
    @EntityGraph(attributePaths = {"student", "classEntity"})
    Page<StudentEnrollment> findAll(Specification<StudentEnrollment> spec, Pageable pageable);

    boolean existsByStudent_IdAndClassEntity_Id(Long studentId, Long classId);

    java.util.Optional<StudentEnrollment> findByStudent_IdAndClassEntity_Id(Long studentId, Long classId);

    @Query("SELECT se.classEntity.id FROM StudentEnrollment se WHERE se.student.id = :studentId")
    List<Long> findClassIdsByStudentId(@Param("studentId") Long studentId);

    @Query("""
            SELECT COUNT(se) > 0
            FROM StudentEnrollment se
            WHERE se.student.id = :studentId
              AND (
                se.status = com.edusys.backend.model.StudentEnrollment.Status.ACTIVE
                OR se.status = com.edusys.backend.model.StudentEnrollment.Status.active
              )
              AND se.classEntity.id IN (
                SELECT ta.classEntity.id
                FROM TeachingAssignment ta
                WHERE ta.teacher.id = :teacherId
                  AND ta.isActive = true
              )
            """)
    boolean existsActiveEnrollmentByTeacherIdAndStudentId(@Param("teacherId") Long teacherId, @Param("studentId") Long studentId);

    @Query("""
            SELECT DISTINCT se.student.id
            FROM StudentEnrollment se
            WHERE (
                se.status = com.edusys.backend.model.StudentEnrollment.Status.ACTIVE
                OR se.status = com.edusys.backend.model.StudentEnrollment.Status.active
              )
              AND se.classEntity.id IN (
                SELECT ta.classEntity.id
                FROM TeachingAssignment ta
                WHERE ta.teacher.id = :teacherId
                  AND ta.isActive = true
              )
            ORDER BY se.student.id
            """)
    List<Long> findActiveStudentIdsByTeacherId(@Param("teacherId") Long teacherId);

    @Query("""
            SELECT new com.edusys.backend.dto.UserClassSummaryDTO(
                se.student.id,
                se.classEntity.id,
                se.classEntity.className,
                se.classEntity.grade,
                se.classEntity.section
            )
            FROM StudentEnrollment se
            WHERE se.student.id IN :studentIds
              AND (
                se.status = com.edusys.backend.model.StudentEnrollment.Status.ACTIVE
                OR se.status = com.edusys.backend.model.StudentEnrollment.Status.active
              )
            ORDER BY se.student.id ASC, se.id DESC
            """)
    List<UserClassSummaryDTO> findActiveClassSummariesByStudentIds(@Param("studentIds") List<Long> studentIds);

    Long countByClassEntityId(Long classEntityId);

    @Query("""
            SELECT se
            FROM StudentEnrollment se
            JOIN FETCH se.classEntity c
            WHERE se.student.id = :studentId
              AND (
                se.status = com.edusys.backend.model.StudentEnrollment.Status.ACTIVE
                OR se.status = com.edusys.backend.model.StudentEnrollment.Status.active
              )
            ORDER BY se.id DESC
            """)
    List<StudentEnrollment> findActiveEnrollmentsByStudentId(@Param("studentId") Long studentId);

    default Optional<StudentEnrollment> findLatestActiveEnrollmentByStudentId(Long studentId) {
        return findActiveEnrollmentsByStudentId(studentId).stream().findFirst();
    }

    @Query("""
            SELECT DISTINCT se.student.id
            FROM StudentEnrollment se
            WHERE se.classEntity.id = :classId
              AND (
                se.status = com.edusys.backend.model.StudentEnrollment.Status.ACTIVE
                OR se.status = com.edusys.backend.model.StudentEnrollment.Status.active
              )
            ORDER BY se.student.id
            """)
    List<Long> findActiveStudentIdsByClassId(@Param("classId") Long classId);
}
