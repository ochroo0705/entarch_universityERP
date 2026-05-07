package com.edusys.backend.repository;

import com.edusys.backend.ai.service.HomeworkWindowMetrics;
import com.edusys.backend.model.HomeworkSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, Long> {
    // Find a student's submission for a specific homework
    Optional<HomeworkSubmission> findByHomeworkIdAndStudentId(Long homeworkId, Long studentId);

    // Find all submissions for a specific homework (teacher view)
    List<HomeworkSubmission> findByHomeworkId(Long homeworkId);

    List<HomeworkSubmission> findByStudent_IdAndHomework_IdIn(Long studentId, Collection<Long> homeworkIds);

    long countByStudent_IdAndStatusIn(Long studentId, Collection<HomeworkSubmission.Status> statuses);

    @Query("""
        SELECT COUNT(h)
        FROM Homework h
        JOIN h.teachingAssignment ta
        JOIN ta.classEntity c
        JOIN c.studentEnrollments se
        WHERE se.student.id = :studentId
          AND (se.status = com.edusys.backend.model.StudentEnrollment.Status.ACTIVE
            OR se.status = com.edusys.backend.model.StudentEnrollment.Status.active)
          AND h.dueDate BETWEEN :startDate AND :endDate
    """)
    long countAssignedHomeworkForStudentBetween(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COUNT(hs)
        FROM HomeworkSubmission hs
        WHERE hs.student.id = :studentId
          AND hs.homework.dueDate BETWEEN :startDate AND :endDate
          AND hs.status = :status
    """)
    long countByStudent_IdAndStatusAndHomework_DueDateBetween(
            @Param("studentId") Long studentId,
            @Param("status") HomeworkSubmission.Status status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COUNT(hs)
        FROM HomeworkSubmission hs
        WHERE hs.student.id = :studentId
          AND hs.homework.dueDate BETWEEN :startDate AND :endDate
          AND hs.status = :status
    """)
    long countByStudent_IdAndStatusWithinDueDateWindow(
            @Param("studentId") Long studentId,
            @Param("status") HomeworkSubmission.Status status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT new com.edusys.backend.ai.service.HomeworkWindowMetrics(
            COUNT(h),
            COALESCE(SUM(CASE WHEN hs.status = :missingStatus THEN 1 ELSE 0 END), 0)
        )
        FROM Homework h
        JOIN h.teachingAssignment ta
        JOIN ta.classEntity c
        JOIN c.studentEnrollments se
        LEFT JOIN HomeworkSubmission hs
            ON hs.homework = h
           AND hs.student.id = :studentId
        WHERE se.student.id = :studentId
          AND (se.status = com.edusys.backend.model.StudentEnrollment.Status.ACTIVE
            OR se.status = com.edusys.backend.model.StudentEnrollment.Status.active)
          AND h.dueDate BETWEEN :startDate AND :endDate
    """)
    HomeworkWindowMetrics summarizeHomeworkWindow(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("missingStatus") HomeworkSubmission.Status missingStatus
    );

}
