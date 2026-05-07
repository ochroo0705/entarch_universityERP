package com.edusys.backend.service;

import com.edusys.backend.dto.HomeworkGradeBulkDto;
import com.edusys.backend.dto.HomeworkGradeDto;
import com.edusys.backend.dto.HomeworkSubmissionResponseDto;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.Homework;
import com.edusys.backend.model.HomeworkSubmission;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.HomeworkRepository;
import com.edusys.backend.repository.HomeworkSubmissionRepository;
import com.edusys.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.edusys.backend.model.HomeworkSubmission.Status.graded;

@Service
public class HomeworkGradingService {

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private final HomeworkSubmissionRepository submissionRepo;
    private final HomeworkRepository homeworkRepo;
    private final UserRepository userRepo;

    public HomeworkGradingService(
            HomeworkSubmissionRepository submissionRepo,
            HomeworkRepository homeworkRepo,
            UserRepository userRepo
    ) {
        this.submissionRepo = submissionRepo;
        this.homeworkRepo = homeworkRepo;
        this.userRepo = userRepo;
    }

    // ------------------------
    // Grade a single submission
    // ------------------------
    public HomeworkSubmissionResponseDto gradeSubmission(
            Long submissionId,
            HomeworkGradeDto dto
    ) {
        User authUser = getCurrentUser();
        // 1. Validate teacher role
        if (!isTeacher(authUser)) {
            throw new AccessDeniedException("Only teachers can grade submissions");
        }

        // 2. Validate submission existence
        HomeworkSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        Homework hw = submission.getHomework();

        // 3. Validate teacher owns the homework
        if (!hw.getTeachingAssignment().getTeacher().getId().equals(authUser.getId())) {
            throw new AccessDeniedException("Teacher does not own this homework");
        }

        // 4. Validate score
        if (dto.score() < 0 || dto.score() > hw.getMaxScore()) {
            throw new IllegalArgumentException("Score must be between 0 and homework max score");
        }

        // 5. Apply grading
        submission.setScore(dto.score());
        submission.setFeedback(dto.feedback());
        submission.setGradedBy(authUser);
        submission.setGradedAt(LocalDateTime.now());
        submission.setStatus(graded);

        return mapToDto(submissionRepo.save(submission));
    }

    // ------------------------
    // Update grade (same as grading)
    // ------------------------
    public HomeworkSubmissionResponseDto updateGrade(
            Long submissionId,
            HomeworkGradeDto dto,
            User authUser
    ) {
        return gradeSubmission(submissionId, dto);
    }

    // ------------------------
    // Bulk grading (optional)
    // ------------------------
    public List<HomeworkSubmissionResponseDto> gradeSubmissionsBulk(
            Long homeworkId,
            List<HomeworkGradeBulkDto> grades
    ) {
        User authUser = getCurrentUser();
        Homework hw = homeworkRepo.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        if (!hw.getTeachingAssignment().getTeacher().getId().equals(authUser.getId())) {
            throw new AccessDeniedException("Teacher does not own this homework");
        }

        List<HomeworkSubmissionResponseDto> results = new ArrayList<>();

        for (HomeworkGradeBulkDto gradeDto : grades) {
            HomeworkSubmission submission = submissionRepo.findById(gradeDto.submissionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Submission not found: " + gradeDto.submissionId()));

            if (gradeDto.score() < 0 || gradeDto.score() > hw.getMaxScore()) {
                throw new IllegalArgumentException(
                        "Score for submission " + gradeDto.submissionId() +
                        " must be between 0 and homework max score");
            }

            submission.setScore(gradeDto.score());
            submission.setFeedback(gradeDto.feedback());
            submission.setGradedBy(authUser);
            submission.setGradedAt(LocalDateTime.now());
            submission.setStatus(graded);

            results.add(mapToDto(submissionRepo.save(submission)));
        }

        return results;
    }

    // ------------------------
    // Helper methods
    // ------------------------
    private boolean isTeacher(User user) {
        return user.getRoleFlags() != null && (user.getRoleFlags() & 2) != 0; // 2 = teacher
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
