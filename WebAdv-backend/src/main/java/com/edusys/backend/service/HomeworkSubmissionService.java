package com.edusys.backend.service;

import com.edusys.backend.dto.HomeworkSubmissionCreateDto;
import com.edusys.backend.dto.HomeworkSubmissionResponseDto;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.Class;
import com.edusys.backend.model.Homework;
import com.edusys.backend.model.HomeworkSubmission;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.HomeworkRepository;
import com.edusys.backend.repository.HomeworkSubmissionRepository;
import com.edusys.backend.repository.ParentStudentRepository;
import com.edusys.backend.repository.StudentEnrollmentRepository;
import com.edusys.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HomeworkSubmissionService {

    private final HomeworkSubmissionRepository submissionRepo;
    private final HomeworkRepository homeworkRepo;
    private final StudentEnrollmentRepository enrollmentRepo;
    private final ParentStudentRepository parentStudentRepository;
    private final UserRepository userRepository;

    public HomeworkSubmissionService(
            HomeworkSubmissionRepository submissionRepo,
            HomeworkRepository homeworkRepo,
            UserRepository userRepo,
            StudentEnrollmentRepository enrollmentRepo,
            ParentStudentRepository parentStudentRepository
    ) {
        this.submissionRepo = submissionRepo;
        this.homeworkRepo = homeworkRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.parentStudentRepository = parentStudentRepository;
        this.userRepository = userRepo;
    }

    // ------------------------
    // Student actions
    // ------------------------

    public HomeworkSubmissionResponseDto submitHomework(
            Long homeworkId,
            HomeworkSubmissionCreateDto dto,
            User authUser
    ) {
        // Students submit for themselves
        if (!isStudent(authUser)) {
            throw new AccessDeniedException("Only students can submit homework");
        }
        if (authUser.getId() == null) {
            throw new AccessDeniedException("Access denied");
        }
        return submitHomeworkForStudentId(homeworkId, dto, authUser.getId(), authUser);
    }

    public HomeworkSubmissionResponseDto submitHomeworkForStudentId(
            Long homeworkId,
            HomeworkSubmissionCreateDto dto,
            Long studentId,
            User authUser
    ) {
        if (studentId == null) {
            throw new IllegalArgumentException("studentId is required");
        }

        // Student submitting for themselves
        boolean isSelf = isStudent(authUser) && authUser.getId().equals(studentId);
        // Parent submitting for linked child
        boolean isLinkedParent = isParent(authUser) && authUser.getId() != null
                && parentStudentRepository.existsByParent_IdAndStudent_Id(authUser.getId(), studentId);

        if (!isSelf && !isLinkedParent) {
            throw new AccessDeniedException("Only the student or a linked parent can submit for this student");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!isStudent(student)) {
            throw new AccessDeniedException("Target user is not a student");
        }

        Homework hw = homeworkRepo.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        if (!isStudentEnrolledInClass(student, hw.getTeachingAssignment().getClassEntity())) {
            throw new AccessDeniedException("Student not enrolled in this class");
        }

        HomeworkSubmission submission = submissionRepo.findByHomeworkIdAndStudentId(homeworkId, studentId)
                .orElse(new HomeworkSubmission());

        submission.setHomework(hw);
        submission.setStudent(student);
        submission.setSubmissionText(dto.submissionText());
        submission.setAttachmentUrl(dto.attachmentUrl());
        submission.setSubmittedAt(LocalDateTime.now());

        submission.setStatus(LocalDate.now().isAfter(hw.getDueDate())
                ? HomeworkSubmission.Status.late
                : HomeworkSubmission.Status.submitted);

        return mapToDto(submissionRepo.save(submission));
    }

    public HomeworkSubmissionResponseDto getMySubmission(Long homeworkId, User authUser) {
        if (!isStudent(authUser)) {
            throw new AccessDeniedException("Only students can view their submissions");
        }

        HomeworkSubmission submission = submissionRepo.findByHomeworkIdAndStudentId(homeworkId, authUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        return mapToDto(submission);
    }

    // ------------------------
    // Teacher actions
    // ------------------------

    public List<HomeworkSubmissionResponseDto> getSubmissionsForHomework(Long homeworkId, User authUser) {
        Homework hw = homeworkRepo.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        if (!isTeacherOwnerOfHomework(authUser, hw)) {
            throw new AccessDeniedException("Teacher does not own this homework");
        }

        return submissionRepo.findByHomeworkId(homeworkId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public HomeworkSubmissionResponseDto getSubmissionForHomeworkAndStudent(Long homeworkId, Long studentId, User authUser) {
        Homework hw = homeworkRepo.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        // Teacher who owns the homework can view any student's submission.
        if (isTeacherOwnerOfHomework(authUser, hw)) {
            HomeworkSubmission submission = submissionRepo.findByHomeworkIdAndStudentId(homeworkId, studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
            return mapToDto(submission);
        }

        // Student can view their own submission.
        if (isStudent(authUser) && authUser.getId() != null && authUser.getId().equals(studentId)) {
            HomeworkSubmission submission = submissionRepo.findByHomeworkIdAndStudentId(homeworkId, studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
            return mapToDto(submission);
        }

        // Linked parent can view the child's submission.
        if (isParent(authUser) && authUser.getId() != null
                && parentStudentRepository.existsByParent_IdAndStudent_Id(authUser.getId(), studentId)) {
            HomeworkSubmission submission = submissionRepo.findByHomeworkIdAndStudentId(homeworkId, studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

            // Optional extra safety: ensure the child is enrolled in the homework class.
            if (!enrollmentRepo.existsByStudent_IdAndClassEntity_Id(studentId, hw.getTeachingAssignment().getClassEntity().getId())) {
                throw new AccessDeniedException("Access denied");
            }

            return mapToDto(submission);
        }

        throw new AccessDeniedException("Access denied");
    }

    public HomeworkSubmissionResponseDto getSubmissionById(Long submissionId, User authUser) {
        HomeworkSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        Homework hw = submission.getHomework();

        Long submissionStudentId = submission.getStudent() != null ? submission.getStudent().getId() : null;

        boolean canAccess = isTeacherOwnerOfHomework(authUser, hw)
                || (submissionStudentId != null && submissionStudentId.equals(authUser.getId()));

        // Linked parent can access the child's submission
        if (!canAccess && isParent(authUser) && authUser.getId() != null && submissionStudentId != null) {
            canAccess = parentStudentRepository.existsByParent_IdAndStudent_Id(authUser.getId(), submissionStudentId);
        }

        if (!canAccess) {
            throw new AccessDeniedException("Access denied");
        }

        return mapToDto(submission);
    }
    public List<HomeworkSubmission> findAll() {
        return submissionRepo.findAll();
    }

    public List<HomeworkSubmissionResponseDto> getSubmissionsForStudentInClass(
            Long classId,
            Long studentId,
            User authUser
    ) {
        // Admin/teacher can access; parent can access linked child; student can access self.
        boolean allowed = isAdmin(authUser) || isTeacherOfClass(authUser, classId);

        if (!allowed && isStudent(authUser) && authUser.getId() != null) {
            allowed = authUser.getId().equals(studentId);
        }

        if (!allowed && isParent(authUser) && authUser.getId() != null) {
            allowed = parentStudentRepository.existsByParent_IdAndStudent_Id(authUser.getId(), studentId);
        }

        if (!allowed) {
            throw new AccessDeniedException("Not authorized to view this student's submissions");
        }

        // Fetch all homeworks in the class
        List<Homework> homeworks = homeworkRepo.findByTeachingAssignment_ClassEntity_Id(classId);

        List<HomeworkSubmissionResponseDto> submissions = new ArrayList<>();
        for (Homework hw : homeworks) {
            submissionRepo.findByHomeworkIdAndStudentId(hw.getId(), studentId)
                    .ifPresent(sub -> submissions.add(mapToDto(sub)));
        }

        return submissions;
    }

    private boolean isTeacherOfClass(User user, Long classId) {
        // Checks if the user is teacher for any assignment in the class
        return homeworkRepo.existsByTeachingAssignment_ClassEntity_IdAndTeachingAssignment_Teacher_Id(classId, user.getId());
    }

    private boolean isAdmin(User user) {
        return user.getRoleFlags() != null && (user.getRoleFlags() & 8) != 0;
    }

    private boolean isParent(User user) {
        return user.getRoleFlags() != null && (user.getRoleFlags() & 4) != 0;
    }




    // ------------------------
    // Helper methods
    // ------------------------

    private boolean isStudent(User user) {
        return user.getRoleFlags() != null && (user.getRoleFlags() & 1) != 0; // 1 = student
    }


    private boolean isTeacher(User user) {
        return user.getRoleFlags() != null && (user.getRoleFlags() & 2) != 0; // 2 = teacher
    }




    private boolean isStudentEnrolledInClass(User student, Class classEntity) {
        return enrollmentRepo.existsByStudent_IdAndClassEntity_Id(student.getId(), classEntity.getId());
    }


    private boolean isTeacherOwnerOfHomework(User teacher, Homework hw) {
        return isTeacher(teacher)
                && hw.getTeachingAssignment().getTeacher().getId().equals(teacher.getId());
    }

    private HomeworkSubmissionResponseDto mapToDto(HomeworkSubmission submission) {
        return new HomeworkSubmissionResponseDto(
                submission.getId(),
                submission.getHomework().getId(),
                submission.getStudent().getId(),
                submission.getSubmissionText(),
                submission.getAttachmentUrl(),
                submission.getSubmittedAt(),
                submission.getScore(),
                submission.getFeedback(),
                submission.getGradedBy() != null ? submission.getGradedBy().getId() : null,
                submission.getGradedAt(),
                submission.getStatus()
        );
    }
}
