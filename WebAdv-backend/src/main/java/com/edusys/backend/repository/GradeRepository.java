package com.edusys.backend.repository;

import com.edusys.backend.ai.service.GradeWindowMetrics;
import com.edusys.backend.model.Grade;
import com.edusys.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    @Query("SELECT g FROM Grade g WHERE g.student.id = :studentId " +
            "ORDER BY g.recordedAt DESC")
    List<Grade> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT AVG(g.gradeValue) FROM Grade g WHERE g.student.id = :studentId")
    Double findAverageGradeValueByStudentId(@Param("studentId") Long studentId);

    @Query("""
        SELECT AVG(g.gradeValue)
        FROM Grade g
        WHERE g.student.id = :studentId
          AND g.recordedAt BETWEEN :from AND :to
    """)
    Double findAverageGradeValueByStudentIdWithinWindow(
            @Param("studentId") Long studentId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT COUNT(g)
        FROM Grade g
        WHERE g.student.id = :studentId
          AND g.recordedAt BETWEEN :from AND :to
    """)
    long countByStudentIdWithinWindow(
            @Param("studentId") Long studentId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT new com.edusys.backend.ai.service.GradeWindowMetrics(
            AVG(g.gradeValue),
            COUNT(g)
        )
        FROM Grade g
        WHERE g.student.id = :studentId
          AND g.recordedAt BETWEEN :from AND :to
    """)
    GradeWindowMetrics summarizeStudentWindow(
            @Param("studentId") Long studentId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT g FROM Grade g WHERE g.student.id = :studentId " +
            "AND g.quarter = :quarter " +
            "ORDER BY g.recordedAt DESC")
    List<Grade> findByStudentIdAndQuarter(
            @Param("studentId") Long studentId,
            @Param("quarter") Integer quarter
    );

    Optional<Grade> findFirstByStudentOrderByRecordedAtDesc(User student);
}
