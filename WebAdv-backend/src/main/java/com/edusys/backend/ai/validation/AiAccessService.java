package com.edusys.backend.ai.validation;

import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.StudentEnrollmentRepository;
import com.edusys.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAccessService {

    private final UserRepository userRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;

    public AiAccessService(UserRepository userRepository, StudentEnrollmentRepository studentEnrollmentRepository) {
        this.userRepository = userRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
    }

    public User requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    public void ensureCanAccessStudent(User actor, Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("studentId is required");
        }
        if (actor.isAdmin()) {
            return;
        }
        if (actor.isTeacher() && studentEnrollmentRepository.existsActiveEnrollmentByTeacherIdAndStudentId(actor.getId(), studentId)) {
            return;
        }
        throw new AccessDeniedException("You do not have access to this student");
    }

    public List<Long> getAccessibleStudentIds(User actor) {
        if (actor.isAdmin()) {
            return userRepository.findAll().stream()
                    .filter(User::isStudent)
                    .map(User::getId)
                    .toList();
        }
        if (actor.isTeacher()) {
            return studentEnrollmentRepository.findActiveStudentIdsByTeacherId(actor.getId());
        }
        return List.of();
    }

    public void ensureCanAccessClassSummary(User actor, Long classId) {
        if (classId == null) {
            throw new IllegalArgumentException("classId is required");
        }
        if (actor.isAdmin()) {
            return;
        }
        if (actor.isTeacher() && studentEnrollmentRepository.findActiveStudentIdsByClassId(classId).stream()
                .anyMatch(studentId -> studentEnrollmentRepository.existsActiveEnrollmentByTeacherIdAndStudentId(actor.getId(), studentId))) {
            return;
        }
        throw new AccessDeniedException("You do not have access to this class summary");
    }

    public void ensureCanAccessTeacherOverview(User actor) {
        if (actor.isTeacher() && !actor.isAdmin()) {
            return;
        }
        if (actor.isAdmin()) {
            return;
        }
        throw new AccessDeniedException("You do not have access to this teacher overview");
    }

    public void ensureCanAccessGradeSummary(User actor, Integer gradeLevel) {
        if (gradeLevel == null) {
            throw new IllegalArgumentException("gradeLevel is required");
        }
        if (!actor.isAdmin()) {
            throw new AccessDeniedException("Only admins can view grade summaries");
        }
    }

    public void ensureCanAccessSchoolSummary(User actor) {
        if (!actor.isAdmin()) {
            throw new AccessDeniedException("Only admins can view school summaries");
        }
    }
}
