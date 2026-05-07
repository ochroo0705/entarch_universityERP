package com.edusys.backend.service;

import com.edusys.backend.dto.*;
import com.edusys.backend.model.*;
import com.edusys.backend.repository.*;
import com.edusys.backend.exception.ResourceNotFoundException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;
    private final UserRepository userRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;

    public GradeService(
            GradeRepository gradeRepository,
            UserRepository userRepository,
            TeachingAssignmentRepository teachingAssignmentRepository) {

        this.gradeRepository = gradeRepository;
        this.userRepository = userRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
    }

    private void validateGrade(GradeRequestDTO request) {
        if (request.gradeValue() < 0 || request.gradeValue() > 100) {
            throw new IllegalArgumentException("Grade value must be between 0 and 100");
        }

        if (request.quarter() < 1 || request.quarter() > 4) {
            throw new IllegalArgumentException("Quarter must be between 1 and 4");
        }
    }

    /* =========================================================
       ASSIGN GRADE
       ========================================================= */
    @Transactional
    public GradeResponseDTO assignGrade(GradeRequestDTO request) {
        User teacher = getCurrentUser();

        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        TeachingAssignment teachingAssignment = teachingAssignmentRepository
                .findById(request.teachingAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found"));

        // SECURITY CHECK: teacher must be assigned to this teaching assignment
        if (!teachingAssignment.getTeacher().getId().equals(teacher.getId())) {
            throw new AccessDeniedException("You are not the teacher of this subject assignment");
        }

        // SECURITY CHECK: student must belong to the class of this assignment
        boolean isStudentInClass = teachingAssignment.getClassEntity()
                .getStudentEnrollments()
                .stream()
                .anyMatch(e -> e.getStudent().getId().equals(student.getId()) &&
                        e.getStatus() == StudentEnrollment.Status.active);

        if (!isStudentInClass) {
            throw new AccessDeniedException("Student does not belong to the class of this teaching assignment");
        }

        validateGrade(request);

        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setTeachingAssignment(teachingAssignment);
        grade.setQuarter(request.quarter());
        grade.setGradeValue(request.gradeValue());
        grade.setGradeType(mapToEntityGradeType(request.gradeType()));
        grade.setRecordedBy(teacher);
        grade.setRecordedAt(LocalDateTime.now());

        Grade saved = gradeRepository.save(grade);
        return mapToResponseDTO(saved);
    }

    /* =========================================================
       UPDATE GRADE
       ========================================================= */
    @Transactional
    public GradeResponseDTO updateGrade(Long gradeId, GradeRequestDTO request) {

        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found"));

        User teacher = getCurrentUser();

        validateGrade(request.gradeValue(), request.gradeType());

        grade.setQuarter(request.quarter());
        grade.setGradeValue(request.gradeValue());
        grade.setGradeType(mapToEntityGradeType(request.gradeType()));
        grade.setRecordedBy(teacher);
        grade.setRecordedAt(LocalDateTime.now());

        return mapToResponseDTO(gradeRepository.save(grade));
    }

    /* =========================================================
       STUDENT GRADES
       ========================================================= */
    public List<GradeResponseDTO> getStudentGrades(Long studentId, Integer quarter) {

        List<Grade> grades = quarter != null
                ? gradeRepository.findByStudentIdAndQuarter(studentId, quarter)
                : gradeRepository.findByStudentId(studentId);

        return grades.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /* =========================================================
       STUDENT GPA
       ========================================================= */
    public StudentGPADTO calculateStudentGPA(Long studentId, Integer quarter) {

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<Grade> grades = gradeRepository
                .findByStudentIdAndQuarter(studentId, quarter);

        if (grades.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No grades found for student in quarter " + quarter);
        }

        double gpa = grades.stream()
                .mapToInt(Grade::getGradeValue)
                .average()
                .orElse(0.0);

        List<StudentGPADTO.SubjectGrade> subjectGrades = grades.stream()
                .map(g -> new StudentGPADTO.SubjectGrade(
                        g.getTeachingAssignment().getSubject().getName(),
                        g.getGradeValue(),
                        g.getGradeType().name()
                ))
                .collect(Collectors.toList());

        return new StudentGPADTO(
                studentId,
                student.getFirstName() + " " + student.getLastName(),
                getStudentClassName(student),
                quarter,
                grades.get(0).getTeachingAssignment().getAcademicYear(),
                Math.round(gpa * 100.0) / 100.0,
                subjectGrades,
                calculatePerformance(gpa)
        );
    }

    /* =========================================================
       GRADE TRENDS
       ========================================================= */
    public GradeTrendsDTO getGradeTrends(Long studentId) {

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<Grade> grades = gradeRepository.findByStudentId(studentId);

        if (grades.isEmpty()) {
            throw new ResourceNotFoundException("No grades found for student");
        }

        Map<Integer, List<Grade>> byQuarter =
                grades.stream().collect(Collectors.groupingBy(Grade::getQuarter));

        List<QuarterTrend> quarterTrends = new ArrayList<>();

        for (Integer quarter : new TreeSet<>(byQuarter.keySet())) {

            List<Grade> qGrades = byQuarter.get(quarter);

            double gpa = qGrades.stream()
                    .mapToInt(Grade::getGradeValue)
                    .average()
                    .orElse(0.0);

            quarterTrends.add(
                    new QuarterTrend(
                            quarter,
                            Math.round(gpa * 100.0) / 100.0,
                            calculatePerformance(gpa)
                    )
            );
        }

        double improvement = quarterTrends.size() >= 2
                ? quarterTrends.get(quarterTrends.size() - 1).gpa()
                - quarterTrends.get(0).gpa()
                : 0.0;

        String trend =
                improvement > 0 ? "IMPROVING" :
                        improvement < 0 ? "DECLINING" : "STABLE";

        return new GradeTrendsDTO(
                studentId,
                student.getFirstName() + " " + student.getLastName(),
                getStudentClassName(student),
                quarterTrends,
                trend,
                Math.round(improvement * 100.0) / 100.0
        );
    }

    /* =========================================================
       VALIDATION
       ========================================================= */
    private void validateGrade(Integer gradeValue, GradeRequestDTO.GradeType type) {

        if (gradeValue == null || gradeValue < 0 || gradeValue > 100) {
            throw new IllegalArgumentException(
                    "Grade value must be between 0 and 100");
        }

        if (type == null) {
            throw new IllegalArgumentException("Grade type is required");
        }
    }

    /* =========================================================
       HELPERS
       ========================================================= */
    private User getCurrentUser() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    private String calculatePerformance(double gpa) {

        if (gpa >= 90) return "EXCELLENT";
        if (gpa >= 80) return "GOOD";
        if (gpa >= 70) return "SATISFACTORY";
        return "NEEDS_IMPROVEMENT";
    }

    private String getStudentClassName(User student) {

        return gradeRepository
                .findFirstByStudentOrderByRecordedAtDesc(student)
                .map(g -> g.getTeachingAssignment()
                        .getClassEntity().getClassName())
                .orElse("N/A");
    }

    private Grade.GradeType mapToEntityGradeType(
            GradeRequestDTO.GradeType dto) {

        return switch (dto) {
            case QUARTER -> Grade.GradeType.QUARTER;
            case MIDTERM -> Grade.GradeType.MIDTERM;
            case FINAL -> Grade.GradeType.FINAL;
            case YEARLY -> Grade.GradeType.YEARLY;
        };
    }

    private GradeResponseDTO mapToResponseDTO(Grade grade) {

        return new GradeResponseDTO(
                grade.getId(),
                grade.getStudent().getId(),
                grade.getStudent().getFirstName() + " " +
                        grade.getStudent().getLastName(),
                grade.getTeachingAssignment().getId(),
                grade.getTeachingAssignment().getSubject().getName(),
                grade.getTeachingAssignment().getClassEntity().getClassName(),
                grade.getQuarter(),
                grade.getGradeValue(),
                mapToDTOGradeType(grade.getGradeType()),
                grade.getRecordedBy() != null
                        ? grade.getRecordedBy().getFirstName() + " " +
                        grade.getRecordedBy().getLastName()
                        : "N/A",
                grade.getRecordedAt()
        );
    }

    private GradeResponseDTO.GradeType mapToDTOGradeType(
            Grade.GradeType type) {

        return switch (type) {
            case QUARTER -> GradeResponseDTO.GradeType.QUARTER;
            case MIDTERM -> GradeResponseDTO.GradeType.MIDTERM;
            case FINAL -> GradeResponseDTO.GradeType.FINAL;
            case YEARLY -> GradeResponseDTO.GradeType.YEARLY;
        };
    }
}