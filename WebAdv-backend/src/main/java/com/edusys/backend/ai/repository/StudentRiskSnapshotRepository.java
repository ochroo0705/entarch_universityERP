package com.edusys.backend.ai.repository;

import com.edusys.backend.ai.model.StudentRiskSnapshot;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRiskSnapshotRepository extends JpaRepository<StudentRiskSnapshot, Long>, JpaSpecificationExecutor<StudentRiskSnapshot> {

    @Override
    @EntityGraph(attributePaths = {"student", "createdByUser", "reviewedByUser"})
    List<StudentRiskSnapshot> findAll();

    @EntityGraph(attributePaths = {"student", "createdByUser", "reviewedByUser"})
    List<StudentRiskSnapshot> findByStudent_IdOrderByCalculatedAtDesc(Long studentId);

    @EntityGraph(attributePaths = {"student", "createdByUser", "reviewedByUser"})
    List<StudentRiskSnapshot> findByStudent_IdInOrderByCalculatedAtDesc(List<Long> studentIds);

    @EntityGraph(attributePaths = {"student", "createdByUser", "reviewedByUser", "classEntity", "indicatorSnapshots"})
    java.util.Optional<StudentRiskSnapshot> findFirstByStudent_IdOrderByCalculatedAtDesc(Long studentId);

    @EntityGraph(attributePaths = {"student", "createdByUser", "reviewedByUser", "classEntity", "indicatorSnapshots"})
    List<StudentRiskSnapshot> findTop10ByStudent_IdOrderByCalculatedAtDesc(Long studentId);

    @Query("""
        SELECT DISTINCT snapshot
        FROM StudentRiskSnapshot snapshot
        LEFT JOIN FETCH snapshot.student
        LEFT JOIN FETCH snapshot.createdByUser
        LEFT JOIN FETCH snapshot.reviewedByUser
        LEFT JOIN FETCH snapshot.classEntity
        LEFT JOIN FETCH snapshot.indicatorSnapshots
        WHERE NOT EXISTS (
            SELECT 1
            FROM StudentRiskSnapshot newer
            WHERE newer.student.id = snapshot.student.id
              AND (
                  newer.calculatedAt > snapshot.calculatedAt
                  OR (newer.calculatedAt = snapshot.calculatedAt AND newer.id > snapshot.id)
              )
        )
        ORDER BY snapshot.calculatedAt DESC, snapshot.id DESC
    """)
    List<StudentRiskSnapshot> findLatestSnapshots();

    @Query("""
        SELECT DISTINCT snapshot
        FROM StudentRiskSnapshot snapshot
        LEFT JOIN FETCH snapshot.student
        LEFT JOIN FETCH snapshot.createdByUser
        LEFT JOIN FETCH snapshot.reviewedByUser
        LEFT JOIN FETCH snapshot.classEntity
        LEFT JOIN FETCH snapshot.indicatorSnapshots
        WHERE snapshot.student.id IN :studentIds
          AND NOT EXISTS (
              SELECT 1
              FROM StudentRiskSnapshot newer
              WHERE newer.student.id = snapshot.student.id
                AND (
                    newer.calculatedAt > snapshot.calculatedAt
                    OR (newer.calculatedAt = snapshot.calculatedAt AND newer.id > snapshot.id)
                )
          )
        ORDER BY snapshot.calculatedAt DESC, snapshot.id DESC
    """)
    List<StudentRiskSnapshot> findLatestSnapshotsByStudentIds(@Param("studentIds") List<Long> studentIds);
}
