package com.edusys.backend.repository;

import com.edusys.backend.model.ExamResult;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "examSchedule",
            "examSchedule.teachingAssignment",
            "examSchedule.teachingAssignment.teacher",
            "examSchedule.teachingAssignment.subject",
            "examSchedule.teachingAssignment.classEntity",
            "student",
            "recordedBy"
    })
    Optional<ExamResult> findById(Long id);

    @EntityGraph(attributePaths = {
            "examSchedule",
            "examSchedule.teachingAssignment",
            "examSchedule.teachingAssignment.teacher",
            "examSchedule.teachingAssignment.subject",
            "examSchedule.teachingAssignment.classEntity",
            "student",
            "recordedBy"
    })
    Optional<ExamResult> findByExamSchedule_IdAndStudent_Id(Long examScheduleId, Long studentId);

    @Query("""
        SELECT er FROM ExamResult er
        JOIN FETCH er.examSchedule es
        JOIN FETCH es.teachingAssignment ta
        JOIN FETCH ta.teacher
        JOIN FETCH ta.subject
        JOIN FETCH ta.classEntity
        JOIN FETCH er.student
        LEFT JOIN FETCH er.recordedBy
        WHERE es.id = :examScheduleId
        ORDER BY er.student.firstName, er.student.lastName, er.id
    """)
    List<ExamResult> findByExamScheduleId(@Param("examScheduleId") Long examScheduleId);

    @Query("""
        SELECT er FROM ExamResult er
        JOIN FETCH er.examSchedule es
        JOIN FETCH es.teachingAssignment ta
        JOIN FETCH ta.teacher
        JOIN FETCH ta.subject
        JOIN FETCH ta.classEntity
        JOIN FETCH er.student
        LEFT JOIN FETCH er.recordedBy
        WHERE er.student.id = :studentId
          AND (:publishedOnly = false OR er.published = true)
        ORDER BY es.examDate DESC, es.startTime DESC, er.id DESC
    """)
    List<ExamResult> findByStudentId(@Param("studentId") Long studentId, @Param("publishedOnly") boolean publishedOnly);

    @Query("""
        SELECT er FROM ExamResult er
        JOIN FETCH er.examSchedule es
        JOIN FETCH es.teachingAssignment ta
        JOIN FETCH ta.teacher
        JOIN FETCH ta.subject
        JOIN FETCH ta.classEntity
        JOIN FETCH er.student
        LEFT JOIN FETCH er.recordedBy
        WHERE ta.teacher.id = :teacherId
        ORDER BY es.examDate DESC, es.startTime DESC, er.student.firstName, er.student.lastName, er.id
    """)
    List<ExamResult> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("""
        SELECT er FROM ExamResult er
        JOIN FETCH er.examSchedule es
        JOIN FETCH es.teachingAssignment ta
        JOIN FETCH ta.teacher
        JOIN FETCH ta.subject
        JOIN FETCH ta.classEntity
        JOIN FETCH er.student
        LEFT JOIN FETCH er.recordedBy
        WHERE ta.id = :teachingAssignmentId
        ORDER BY es.examDate DESC, es.startTime DESC, er.student.firstName, er.student.lastName, er.id
    """)
    List<ExamResult> findByTeachingAssignmentId(@Param("teachingAssignmentId") Long teachingAssignmentId);

    @Query("""
        SELECT er FROM ExamResult er
        JOIN FETCH er.examSchedule es
        JOIN FETCH es.teachingAssignment ta
        JOIN FETCH ta.teacher
        JOIN FETCH ta.subject
        JOIN FETCH ta.classEntity
        JOIN FETCH er.student
        LEFT JOIN FETCH er.recordedBy
        WHERE ta.classEntity.id = :classId
        ORDER BY es.examDate DESC, es.startTime DESC, er.student.firstName, er.student.lastName, er.id
    """)
    List<ExamResult> findByClassId(@Param("classId") Long classId);

    @Query("""
        SELECT er FROM ExamResult er
        JOIN FETCH er.examSchedule es
        JOIN FETCH es.teachingAssignment ta
        JOIN FETCH ta.teacher
        JOIN FETCH ta.subject
        JOIN FETCH ta.classEntity
        JOIN FETCH er.student
        LEFT JOIN FETCH er.recordedBy
        ORDER BY es.examDate DESC, es.startTime DESC, er.student.firstName, er.student.lastName, er.id
    """)
    List<ExamResult> findAllDetailed();
}
