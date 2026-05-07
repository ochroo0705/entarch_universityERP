package com.edusys.backend.service;

import com.edusys.backend.dto.ExamResultPublishRequestDTO;
import com.edusys.backend.dto.ExamResultResponseDTO;
import com.edusys.backend.dto.ExamResultUpsertRequestDTO;
import com.edusys.backend.dto.ExamRosterItemDTO;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.ExamResult;
import com.edusys.backend.model.ExamSchedule;
import com.edusys.backend.model.StudentEnrollment;
import com.edusys.backend.model.TeachingAssignment;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.ExamResultRepository;
import com.edusys.backend.repository.ExamScheduleRepository;
import com.edusys.backend.repository.TeachingAssignmentRepository;
import com.edusys.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ExamResultService {

    private final ExamResultRepository examResultRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final UserRepository userRepository;
    private final StudentAccessService studentAccessService;

    public ExamResultService(
            ExamResultRepository examResultRepository,
            ExamScheduleRepository examScheduleRepository,
            TeachingAssignmentRepository teachingAssignmentRepository,
            UserRepository userRepository,
            StudentAccessService studentAccessService
    ) {
        this.examResultRepository = examResultRepository;
        this.examScheduleRepository = examScheduleRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
        this.userRepository = userRepository;
        this.studentAccessService = studentAccessService;
    }

    @Transactional
    public ExamResultResponseDTO upsertExamResult(ExamResultUpsertRequestDTO request) {
        User actor = getCurrentUser();
        ExamSchedule examSchedule = getExamSchedule(request.examScheduleId());
        TeachingAssignment assignment = examSchedule.getTeachingAssignment();
        ensureCanManageResults(actor, assignment);

        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        ensureStudentBelongsToAssignment(student, assignment);
        validateScores(request.score(), request.totalScore(), request.weighting());

        ExamResult examResult = examResultRepository
                .findByExamSchedule_IdAndStudent_Id(request.examScheduleId(), request.studentId())
                .orElseGet(ExamResult::new);

        examResult.setExamSchedule(examSchedule);
        examResult.setStudent(student);
        examResult.setScore(scale(request.score()));
        examResult.setTotalScore(scale(request.totalScore()));
        examResult.setPercentage(calculatePercentage(request.score(), request.totalScore()));
        examResult.setWeighting(request.weighting() == null ? null : scale(request.weighting()));
        examResult.setTeacherComment(normalizeText(request.teacherComment()));
        examResult.setRemarks(normalizeText(request.remarks()));
        examResult.setPublished(Boolean.TRUE.equals(request.published()));
        examResult.setRecordedBy(actor);

        return mapToResponseDTO(examResultRepository.save(examResult));
    }

    @Transactional
    public ExamResultResponseDTO updatePublishStatus(Long examResultId, ExamResultPublishRequestDTO request) {
        User actor = getCurrentUser();
        ExamResult examResult = getExamResultEntity(examResultId);
        ensureCanManageResults(actor, examResult.getExamSchedule().getTeachingAssignment());
        examResult.setPublished(Boolean.TRUE.equals(request.published()));
        examResult.setRecordedBy(actor);
        return mapToResponseDTO(examResultRepository.save(examResult));
    }

    @Transactional(readOnly = true)
    public ExamResultResponseDTO getExamResult(Long examResultId) {
        return mapToResponseDTO(getExamResultEntityWithAccess(examResultId));
    }

    @Transactional(readOnly = true)
    public List<ExamResultResponseDTO> getExamResultsByExamSchedule(Long examScheduleId) {
        User actor = getCurrentUser();
        ExamSchedule examSchedule = getExamSchedule(examScheduleId);
        List<ExamResult> results = examResultRepository.findByExamScheduleId(examScheduleId);
        if (actor.isTeacher() || actor.isAdmin()) {
            ensureCanReadExamResults(actor, examSchedule.getTeachingAssignment(), null);
            return results.stream().map(this::mapToResponseDTO).toList();
        }

        return results.stream()
                .filter(result -> Boolean.TRUE.equals(result.getPublished()) && studentAccessService.canAccessStudent(result.getStudent().getId()))
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamRosterItemDTO> getExamScheduleRoster(Long examScheduleId) {
        User actor = getCurrentUser();
        ExamSchedule examSchedule = getExamSchedule(examScheduleId);
        ensureCanReadExamResults(actor, examSchedule.getTeachingAssignment(), null);

        List<ExamResult> results = examResultRepository.findByExamScheduleId(examScheduleId);
        TeachingAssignment assignment = examSchedule.getTeachingAssignment();

        return assignment.getClassEntity().getStudentEnrollments().stream()
                .filter(enrollment -> enrollment.getStatus() == StudentEnrollment.Status.active)
                .map(StudentEnrollment::getStudent)
                .map(student -> {
                    ExamResult existing = results.stream()
                            .filter(result -> result.getStudent().getId().equals(student.getId()))
                            .findFirst()
                            .orElse(null);
                    return new ExamRosterItemDTO(
                            examSchedule.getId(),
                            existing == null ? null : existing.getId(),
                            student.getId(),
                            student.getFullName().trim(),
                            examSchedule.getTitle(),
                            examSchedule.getExamDate().toString(),
                            assignment.getSubject().getName(),
                            assignment.getClassEntity().getClassName(),
                            existing == null ? null : existing.getScore(),
                            existing == null ? null : existing.getTotalScore(),
                            existing == null ? null : existing.getPercentage(),
                            existing == null ? null : existing.getWeighting(),
                            existing == null ? null : existing.getTeacherComment(),
                            existing == null ? null : existing.getRemarks(),
                            existing == null ? Boolean.FALSE : existing.getPublished()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamResultResponseDTO> getStudentExamResults(Long studentId) {
        User actor = getCurrentUser();
        if (!studentAccessService.canAccessStudent(studentId) && !(actor.isTeacher() || actor.isAdmin())) {
            throw new AccessDeniedException("Access denied");
        }
        boolean publishedOnly = !(actor.isTeacher() || actor.isAdmin());
        return examResultRepository.findByStudentId(studentId, publishedOnly).stream()
                .filter(result -> canSeeResult(actor, result))
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamResultResponseDTO> getTeacherExamResults(Long teacherId) {
        User actor = getCurrentUser();
        if (!actor.isAdmin() && !actor.getId().equals(teacherId)) {
            throw new AccessDeniedException("Access denied");
        }
        return examResultRepository.findByTeacherId(teacherId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamRosterItemDTO> getTeachingAssignmentExamRoster(Long teachingAssignmentId) {
        User actor = getCurrentUser();
        TeachingAssignment assignment = teachingAssignmentRepository.findById(teachingAssignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found"));
        ensureCanReadExamResults(actor, assignment, null);

        return examResultRepository.findByTeachingAssignmentId(teachingAssignmentId).stream()
                .map(this::mapToRosterItemDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamRosterItemDTO> getClassExamRoster(Long classId) {
        User actor = getCurrentUser();
        if (!actor.isAdmin() && !actor.isTeacher()) {
            throw new AccessDeniedException("Access denied");
        }
        List<ExamResult> results = actor.isAdmin()
                ? examResultRepository.findByClassId(classId)
                : examResultRepository.findByTeacherId(actor.getId()).stream()
                .filter(result -> result.getExamSchedule().getTeachingAssignment().getClassEntity().getId().equals(classId))
                .toList();

        return results.stream().map(this::mapToRosterItemDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ExamResultResponseDTO> getAllExamResults() {
        User actor = getCurrentUser();
        if (!actor.isAdmin()) {
            throw new AccessDeniedException("Access denied");
        }
        return examResultRepository.findAllDetailed().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private ExamResult getExamResultEntityWithAccess(Long examResultId) {
        User actor = getCurrentUser();
        ExamResult examResult = getExamResultEntity(examResultId);
        if (actor.isTeacher() || actor.isAdmin()) {
            ensureCanReadExamResults(actor, examResult.getExamSchedule().getTeachingAssignment(), examResult);
            return examResult;
        }
        if (!Boolean.TRUE.equals(examResult.getPublished()) || !studentAccessService.canAccessStudent(examResult.getStudent().getId())) {
            throw new AccessDeniedException("Access denied");
        }
        return examResult;
    }

    private ExamResult getExamResultEntity(Long examResultId) {
        return examResultRepository.findById(examResultId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam result not found"));
    }

    private ExamSchedule getExamSchedule(Long examScheduleId) {
        return examScheduleRepository.findById(examScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam schedule not found"));
    }

    private void ensureCanManageResults(User actor, TeachingAssignment assignment) {
        if (actor.isAdmin()) {
            return;
        }
        if (!actor.isTeacher() || !assignment.getTeacher().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Only the owning teacher or an admin can manage exam results");
        }
    }

    private void ensureCanReadExamResults(User actor, TeachingAssignment assignment, ExamResult examResult) {
        if (actor.isAdmin()) {
            return;
        }
        if (!actor.isTeacher() || !assignment.getTeacher().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private boolean canSeeResult(User actor, ExamResult examResult) {
        if (actor.isAdmin()) {
            return true;
        }
        if (actor.isTeacher()) {
            return examResult.getExamSchedule().getTeachingAssignment().getTeacher().getId().equals(actor.getId());
        }
        return Boolean.TRUE.equals(examResult.getPublished())
                && studentAccessService.canAccessStudent(examResult.getStudent().getId());
    }

    private void ensureStudentBelongsToAssignment(User student, TeachingAssignment assignment) {
        boolean isStudentInClass = assignment.getClassEntity()
                .getStudentEnrollments()
                .stream()
                .anyMatch(enrollment -> enrollment.getStudent().getId().equals(student.getId())
                        && enrollment.getStatus() == StudentEnrollment.Status.active);

        if (!isStudentInClass) {
            throw new AccessDeniedException("Student does not belong to the class of this exam");
        }
    }

    private void validateScores(BigDecimal score, BigDecimal totalScore, BigDecimal weighting) {
        if (score == null || totalScore == null) {
            throw new IllegalArgumentException("Score and total score are required");
        }
        if (score.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Score must be non-negative");
        }
        if (totalScore.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total score must be greater than zero");
        }
        if (score.compareTo(totalScore) > 0) {
            throw new IllegalArgumentException("Score cannot exceed total score");
        }
        if (weighting != null && (weighting.compareTo(BigDecimal.ZERO) < 0 || weighting.compareTo(new BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException("Weighting must be between 0 and 100");
        }
    }

    private BigDecimal calculatePercentage(BigDecimal score, BigDecimal totalScore) {
        return score.multiply(new BigDecimal("100")).divide(totalScore, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ExamResultResponseDTO mapToResponseDTO(ExamResult examResult) {
        ExamSchedule examSchedule = examResult.getExamSchedule();
        TeachingAssignment assignment = examSchedule.getTeachingAssignment();
        User student = examResult.getStudent();
        User teacher = assignment.getTeacher();

        return new ExamResultResponseDTO(
                examResult.getId(),
                examSchedule.getId(),
                assignment.getId(),
                student.getId(),
                student.getFullName().trim(),
                teacher.getId(),
                teacher.getFullName().trim(),
                assignment.getClassEntity().getId(),
                assignment.getClassEntity().getClassName(),
                assignment.getSubject().getId(),
                assignment.getSubject().getName(),
                examSchedule.getTitle(),
                examSchedule.getExamDate().toString(),
                examSchedule.getStartTime().toString(),
                examSchedule.getEndTime().toString(),
                examSchedule.getRoomNumber(),
                examSchedule.getNotes(),
                examResult.getScore(),
                examResult.getTotalScore(),
                examResult.getPercentage(),
                examResult.getWeighting(),
                examResult.getTeacherComment(),
                examResult.getRemarks(),
                examResult.getPublished(),
                examResult.getCreatedAt() == null ? null : examResult.getCreatedAt().toString(),
                examResult.getUpdatedAt() == null ? null : examResult.getUpdatedAt().toString()
        );
    }

    private ExamRosterItemDTO mapToRosterItemDTO(ExamResult examResult) {
        ExamSchedule examSchedule = examResult.getExamSchedule();
        TeachingAssignment assignment = examSchedule.getTeachingAssignment();
        return new ExamRosterItemDTO(
                examSchedule.getId(),
                examResult.getId(),
                examResult.getStudent().getId(),
                examResult.getStudent().getFullName().trim(),
                examSchedule.getTitle(),
                examSchedule.getExamDate().toString(),
                assignment.getSubject().getName(),
                assignment.getClassEntity().getClassName(),
                examResult.getScore(),
                examResult.getTotalScore(),
                examResult.getPercentage(),
                examResult.getWeighting(),
                examResult.getTeacherComment(),
                examResult.getRemarks(),
                examResult.getPublished()
        );
    }
}
