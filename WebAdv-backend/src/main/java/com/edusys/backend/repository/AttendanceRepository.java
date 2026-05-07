package com.edusys.backend.repository;

import com.edusys.backend.model.Attendance;
import com.edusys.backend.model.User;
import com.edusys.backend.ai.service.AttendanceWindowMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Collection;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId " +
            "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
            "ORDER BY a.attendanceDate DESC")
    List<Attendance> findByStudentIdAndDateRange(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Optional<Attendance> findFirstByStudentOrderByCreatedAtDesc(User student);

        long countByStudent_Id(Long studentId);

        long countByStudent_IdAndStatusIn(Long studentId, Collection<Attendance.Status> statuses);

    long countByStudent_IdAndAttendanceDateBetween(Long studentId, LocalDate startDate, LocalDate endDate);

    long countByStudent_IdAndStatusInAndAttendanceDateBetween(
            Long studentId,
            Collection<Attendance.Status> statuses,
            LocalDate startDate,
            LocalDate endDate
    );

    long countByStudent_IdAndStatusAndAttendanceDateBetween(
            Long studentId,
            Attendance.Status status,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
            SELECT new com.edusys.backend.ai.service.AttendanceWindowMetrics(
                COUNT(a),
                COALESCE(SUM(CASE WHEN a.status IN :attendedStatuses THEN 1 ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN a.status = :lateStatus THEN 1 ELSE 0 END), 0)
            )
            FROM Attendance a
            WHERE a.student.id = :studentId
              AND a.attendanceDate BETWEEN :startDate AND :endDate
            """)
    AttendanceWindowMetrics summarizeAttendanceWindow(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("attendedStatuses") Collection<Attendance.Status> attendedStatuses,
            @Param("lateStatus") Attendance.Status lateStatus
    );

    @Query("SELECT a FROM Attendance a WHERE a.teachingAssignment.classEntity.id = :classId " +
            "AND a.attendanceDate = :date ORDER BY a.student.lastName, a.student.firstName")
    List<Attendance> findByClassIdAndDate(
            @Param("classId") Long classId,
            @Param("date") LocalDate date);

    @Query("SELECT DISTINCT a.attendanceDate FROM Attendance a " +
            "WHERE a.teachingAssignment.classEntity.id = :classId " +
            "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
            "ORDER BY a.attendanceDate")
    List<LocalDate> findAttendanceDatesByClassIdAndRange(
            @Param("classId") Long classId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Attendance a " +
            "JOIN a.teachingAssignment ta " +
            "JOIN ta.classEntity c " +
            "JOIN c.studentEnrollments e " +
            "WHERE c.id = :classId " +
            "AND e.status = 'active' " +
            "AND a.student.id = e.student.id " +
            "AND a.attendanceDate BETWEEN :startDate AND :endDate " +
            "ORDER BY a.student.lastName, a.student.firstName, a.attendanceDate DESC")
    List<Attendance> findByClassIdAndDateRange(
            @Param("classId") Long classId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
