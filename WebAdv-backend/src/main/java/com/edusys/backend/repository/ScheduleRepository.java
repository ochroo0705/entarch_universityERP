package com.edusys.backend.repository;

import com.edusys.backend.model.Schedule;
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

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long>, JpaSpecificationExecutor<Schedule> {

    @Override
    @EntityGraph(attributePaths = {
            "teachingAssignment",
            "teachingAssignment.teacher",
            "teachingAssignment.subject",
            "teachingAssignment.classEntity"
    })
    Page<Schedule> findAll(Specification<Schedule> spec, Pageable pageable);

    @Query("""
        SELECT s FROM Schedule s
        JOIN s.teachingAssignment ta
        JOIN ta.classEntity c
        JOIN StudentEnrollment e ON e.classEntity = c
        WHERE e.student.id = :studentId AND s.isActive = true
        ORDER BY s.dayOfWeek, s.periodNumber
    """)
    List<Schedule> findByStudentId(@Param("studentId") Long studentId);

    @Query("""
        SELECT COUNT(s) FROM Schedule s
        JOIN s.teachingAssignment ta
        JOIN ta.classEntity c
        JOIN StudentEnrollment e ON e.classEntity = c
        WHERE e.student.id = :studentId
          AND s.isActive = true
          AND s.dayOfWeek = :dayOfWeek
    """)
    long countByStudentIdAndDayOfWeek(
            @Param("studentId") Long studentId,
            @Param("dayOfWeek") Integer dayOfWeek
    );

    @Query("""
        SELECT s FROM Schedule s
        JOIN s.teachingAssignment ta
        WHERE ta.teacher.id = :teacherId AND s.isActive = true
        ORDER BY s.dayOfWeek, s.periodNumber
    """)
    List<Schedule> findByTeacherId(@Param("teacherId") Long teacherId);

    @Query("""
        SELECT s FROM Schedule s
        WHERE s.teachingAssignment.id = :teachingAssignmentId 
        AND s.dayOfWeek = :dayOfWeek 
        AND s.periodNumber = :periodNumber
        AND s.isActive = true
    """)
    List<Schedule> findConflictingSchedules(
        @Param("teachingAssignmentId") Long teachingAssignmentId,
        @Param("dayOfWeek") Integer dayOfWeek,
        @Param("periodNumber") Integer periodNumber
    );

    @Query("""
        SELECT s FROM Schedule s
        JOIN s.teachingAssignment ta
        WHERE ta.teacher.id = :teacherId
        AND s.dayOfWeek = :dayOfWeek
        AND s.periodNumber = :periodNumber
        AND s.isActive = true
    """)
    List<Schedule> findTeacherConflicts(
        @Param("teacherId") Long teacherId,
        @Param("dayOfWeek") Integer dayOfWeek,
        @Param("periodNumber") Integer periodNumber
    );

    @Query("""
        SELECT s FROM Schedule s
        JOIN s.teachingAssignment ta
        WHERE ta.classEntity.id = :classId
        AND s.dayOfWeek = :dayOfWeek
        AND s.periodNumber = :periodNumber
        AND s.isActive = true
    """)
    List<Schedule> findClassConflicts(
        @Param("classId") Long classId,
        @Param("dayOfWeek") Integer dayOfWeek,
        @Param("periodNumber") Integer periodNumber
    );

    @Query("""
        SELECT s FROM Schedule s
        WHERE s.roomNumber = :roomNumber
        AND s.dayOfWeek = :dayOfWeek
        AND s.periodNumber = :periodNumber
        AND s.isActive = true
    """)
    List<Schedule> findRoomConflicts(
        @Param("roomNumber") String roomNumber,
        @Param("dayOfWeek") Integer dayOfWeek,
        @Param("periodNumber") Integer periodNumber
    );

    @Query("""
        SELECT DISTINCT c FROM Class c
        JOIN TeachingAssignment ta ON ta.classEntity = c
        JOIN Schedule s ON s.teachingAssignment = ta
        WHERE ta.teacher.id = :teacherId AND s.isActive = true
        ORDER BY c.grade, c.className
    """)
    List<com.edusys.backend.model.Class> findClassesByTeacherId(@Param("teacherId") Long teacherId);
}
