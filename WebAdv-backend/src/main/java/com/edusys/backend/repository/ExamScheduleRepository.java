package com.edusys.backend.repository;

import com.edusys.backend.model.ExamSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long>, JpaSpecificationExecutor<ExamSchedule> {

    @Override
    @EntityGraph(attributePaths = {
            "teachingAssignment",
            "teachingAssignment.teacher",
            "teachingAssignment.subject",
            "teachingAssignment.classEntity"
    })
    Page<ExamSchedule> findAll(Specification<ExamSchedule> spec, Pageable pageable);

    @EntityGraph(attributePaths = {
            "teachingAssignment",
            "teachingAssignment.teacher",
            "teachingAssignment.subject",
            "teachingAssignment.classEntity"
    })
    List<ExamSchedule> findAll(Specification<ExamSchedule> spec, org.springframework.data.domain.Sort sort);

    @EntityGraph(attributePaths = {
            "teachingAssignment",
            "teachingAssignment.teacher",
            "teachingAssignment.subject",
            "teachingAssignment.classEntity"
    })
    java.util.Optional<ExamSchedule> findById(Long id);

    @Query("""
        SELECT e FROM ExamSchedule e
        JOIN e.teachingAssignment ta
        WHERE ta.teacher.id = :teacherId
          AND e.published = true
          AND e.isActive = true
        ORDER BY e.examDate, e.startTime, e.id
    """)
    List<ExamSchedule> findPublishedByTeacherId(@Param("teacherId") Long teacherId);

    @Query("""
        SELECT e FROM ExamSchedule e
        JOIN e.teachingAssignment ta
        JOIN ta.classEntity c
        JOIN StudentEnrollment se ON se.classEntity = c
        WHERE se.student.id = :studentId
          AND e.published = true
          AND e.isActive = true
        ORDER BY e.examDate, e.startTime, e.id
    """)
    List<ExamSchedule> findPublishedByStudentId(@Param("studentId") Long studentId);

    @Query("""
        SELECT e FROM ExamSchedule e
        JOIN e.teachingAssignment ta
        WHERE ta.classEntity.id = :classId
          AND e.examDate = :examDate
          AND e.isActive = true
          AND (:excludeId IS NULL OR e.id <> :excludeId)
          AND (
              (e.startTime < :endTime AND e.endTime > :startTime)
          )
    """)
    List<ExamSchedule> findClassConflicts(
            @Param("classId") Long classId,
            @Param("examDate") LocalDate examDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}
