package com.edusys.backend.service;

import com.edusys.backend.dto.*;
import com.edusys.backend.model.*;
import com.edusys.backend.model.Class;
import com.edusys.backend.repository.*;
import com.edusys.backend.exception.ResourceNotFoundException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final ClassRepository classRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            UserRepository userRepository,
            TeachingAssignmentRepository teachingAssignmentRepository,
            ClassRepository classRepository) {

        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
        this.classRepository = classRepository;
    }

    /* =========================================================
       SINGLE ATTENDANCE
       ========================================================= */
    @Transactional
    public AttendanceResponseDTO markAttendance(AttendanceRequestDTO request) {

        User teacher = getCurrentUser();

        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        TeachingAssignment teachingAssignment = teachingAssignmentRepository
                .findById(request.teachingAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found"));

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setTeachingAssignment(teachingAssignment);
        attendance.setAttendanceDate(request.attendanceDate());
        attendance.setPeriodNumber(request.periodNumber());
        attendance.setStatus(mapToEntityStatus(request.status()));
        attendance.setRemarks(request.remarks());
        attendance.setMarkedBy(teacher);
        attendance.setCreatedAt(LocalDateTime.now());

        return mapToResponseDTO(attendanceRepository.save(attendance));
    }

    /* =========================================================
       BULK ATTENDANCE
       ========================================================= */
    @Transactional
    public List<AttendanceResponseDTO> markBulkAttendance(
            BulkAttendanceRequestDTO request) {

        User teacher = getCurrentUser();

        TeachingAssignment ta = teachingAssignmentRepository
                .findById(request.teachingAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Teaching assignment not found"));

        if (!ta.getTeacher().getId().equals(teacher.getId())) {
            throw new AccessDeniedException(
                    "Teacher does not own this teaching assignment");
        }

        List<AttendanceResponseDTO> results = new ArrayList<>();

        for (BulkAttendanceItem item : request.attendances()) {

            User student = userRepository.findById(item.studentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Student not found: " + item.studentId()));

            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setTeachingAssignment(ta);
            attendance.setAttendanceDate(request.attendanceDate());
            attendance.setPeriodNumber(request.periodNumber());
            attendance.setStatus(mapToEntityStatus(item.status()));
            attendance.setRemarks(item.remarks());
            attendance.setMarkedBy(teacher);
            attendance.setCreatedAt(LocalDateTime.now());

            results.add(mapToResponseDTO(attendanceRepository.save(attendance)));
        }

        return results;
    }

    /* =========================================================
       STUDENT ATTENDANCE
       ========================================================= */
    public List<AttendanceResponseDTO> getStudentAttendance(
            Long studentId, LocalDate startDate, LocalDate endDate) {

        return attendanceRepository
                .findByStudentIdAndDateRange(studentId, startDate, endDate)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /* =========================================================
       CLASS ATTENDANCE SUMMARY
       ========================================================= */
    public ClassAttendanceSummaryDTO getClassAttendanceSummary(
            Long classId,
            LocalDate startDate,
            LocalDate endDate) {

        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        List<StudentEnrollment> enrollments = classEntity.getStudentEnrollments()
                .stream()
                .filter(e -> e.getStatus() == StudentEnrollment.Status.active)
                .toList();

        // Single batch query instead of N queries per student
        List<Attendance> allRecords = attendanceRepository.findByClassIdAndDateRange(classId, startDate, endDate);
        Map<Long, List<Attendance>> byStudent = allRecords.stream()
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        List<StudentAttendanceSummary> summaries = new ArrayList<>();

        for (StudentEnrollment enrollment : enrollments) {

            List<Attendance> records = byStudent.getOrDefault(enrollment.getStudent().getId(), List.of());

            long present = records.stream()
                    .filter(a -> a.getStatus() == Attendance.Status.present)
                    .count();

            long absent = records.stream()
                    .filter(a -> a.getStatus() == Attendance.Status.absent)
                    .count();

            long late = records.stream()
                    .filter(a -> a.getStatus() == Attendance.Status.late)
                    .count();

            double rate = records.isEmpty() ? 0.0 :
                    (present * 100.0 / records.size());

            summaries.add(new StudentAttendanceSummary(
                    enrollment.getStudent().getId(),
                    enrollment.getStudent().getFullName(),
                    (int) present,
                    (int) absent,
                    (int) late,
                    Math.round(rate * 10.0) / 10.0
            ));
        }

        double avgRate = summaries.stream()
                .mapToDouble(StudentAttendanceSummary::attendanceRate)
                .average()
                .orElse(0.0);

        return new ClassAttendanceSummaryDTO(
                classId,
                classEntity.getClassName(),
                enrollments.size(),
                new DateRange(startDate, endDate),
                new OverallStatistics(
                        (int) ChronoUnit.DAYS.between(startDate, endDate),
                        Math.round(avgRate * 10.0) / 10.0,
                        summaries.stream()
                                .mapToInt(StudentAttendanceSummary::absentDays)
                                .sum(),
                        summaries.stream()
                                .mapToInt(StudentAttendanceSummary::lateDays)
                                .sum()
                ),
                summaries
        );
    }

    /* =========================================================
       CLASS ATTENDANCE BY DATE
       ========================================================= */
    public List<AttendanceResponseDTO> getClassAttendanceByDate(
            Long classId, LocalDate date) {

        classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        return attendanceRepository.findByClassIdAndDate(classId, date)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<LocalDate> getClassAttendanceDates(
            Long classId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findAttendanceDatesByClassIdAndRange(classId, startDate, endDate);
    }

    /* =========================================================
       PARENT WARNING
       ========================================================= */
    public ParentWarningDTO getParentWarning(
            Long studentId, LocalDate startDate, LocalDate endDate) {

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<Attendance> attendances =
                attendanceRepository.findByStudentIdAndDateRange(
                        studentId, startDate, endDate);

        int absent = (int) attendances.stream()
                .filter(a -> a.getStatus() == Attendance.Status.absent)
                .count();

        int late = (int) attendances.stream()
                .filter(a -> a.getStatus() == Attendance.Status.late)
                .count();

        int excused = (int) attendances.stream()
                .filter(a -> a.getStatus() == Attendance.Status.excused)
                .count();

        int sick = (int) attendances.stream()
                .filter(a -> a.getStatus() == Attendance.Status.sick)
                .count();

        String warningLevel = calculateWarningLevel(absent);

        List<ParentWarningDTO.AbsentDetail> recent =
                attendances.stream()
                        .filter(a -> a.getStatus() == Attendance.Status.absent
                                || a.getStatus() == Attendance.Status.late)
                        .sorted((a, b) -> b.getAttendanceDate()
                                .compareTo(a.getAttendanceDate()))
                        .limit(10)
                        .map(a -> new ParentWarningDTO.AbsentDetail(
                                a.getAttendanceDate(),
                                a.getTeachingAssignment()
                                        .getSubject().getName(),
                                a.getStatus().name(),
                                a.getRemarks()
                        ))
                        .toList();

        return new ParentWarningDTO(
                studentId,
                student.getFirstName() + " " + student.getLastName(),
                getStudentClassName(student),
                absent,
                late,
                excused,
                sick,
                startDate,
                endDate,
                warningLevel,
                recent
        );
    }

    /* =========================================================
       HELPERS
       ========================================================= */
    private User getCurrentUser() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found"));
    }

    private String calculateWarningLevel(int totalAbsent) {
        if (totalAbsent >= 10) return "CRITICAL";
        if (totalAbsent >= 7) return "WARNING";
        if (totalAbsent >= 4) return "ATTENTION";
        return "NORMAL";
    }

    private String getStudentClassName(User student) {
        return attendanceRepository
                .findFirstByStudentOrderByCreatedAtDesc(student)
                .map(a -> a.getTeachingAssignment()
                        .getClassEntity().getClassName())
                .orElse("N/A");
    }

    private Attendance.Status mapToEntityStatus(
            AttendanceRequestDTO.AttendanceStatus dto) {

        return switch (dto) {
                        case PRESENT -> Attendance.Status.present;
                        case ABSENT -> Attendance.Status.absent;
                        case LATE -> Attendance.Status.late;
                        case EXCUSED -> Attendance.Status.excused;
                        case SICK -> Attendance.Status.sick;
        };
    }

    private AttendanceResponseDTO mapToResponseDTO(Attendance attendance) {

        return new AttendanceResponseDTO(
                attendance.getId(),
                attendance.getStudent().getId(),
                attendance.getStudent().getFirstName() + " " +
                        attendance.getStudent().getLastName(),
                attendance.getTeachingAssignment().getId(),
                attendance.getTeachingAssignment()
                        .getSubject().getName(),
                attendance.getTeachingAssignment()
                        .getClassEntity().getClassName(),
                attendance.getAttendanceDate(),
                attendance.getPeriodNumber(),
                mapToDTOStatus(attendance.getStatus()),
                attendance.getRemarks(),
                attendance.getMarkedBy() != null
                        ? attendance.getMarkedBy()
                        .getFirstName() + " " +
                        attendance.getMarkedBy().getLastName()
                        : "N/A",
                attendance.getCreatedAt()
        );
    }

    private AttendanceResponseDTO.AttendanceStatus mapToDTOStatus(
            Attendance.Status status) {

        return switch (status) {
            case present -> AttendanceResponseDTO
                    .AttendanceStatus.PRESENT;
            case absent -> AttendanceResponseDTO
                    .AttendanceStatus.ABSENT;
            case late -> AttendanceResponseDTO
                    .AttendanceStatus.LATE;
            case excused -> AttendanceResponseDTO
                    .AttendanceStatus.EXCUSED;
            case sick -> AttendanceResponseDTO
                    .AttendanceStatus.SICK;
        };
    }
}