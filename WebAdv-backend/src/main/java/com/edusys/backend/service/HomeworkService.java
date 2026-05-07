package com.edusys.backend.service;

import com.edusys.backend.dto.HomeworkAttachmentDto;
import com.edusys.backend.dto.HomeworkCreateDto;
import com.edusys.backend.dto.HomeworkResponseDto;
import com.edusys.backend.dto.HomeworkSubmissionResponseDto;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.Homework;
import com.edusys.backend.model.HomeworkAttachment;
import com.edusys.backend.model.HomeworkSubmission;
import com.edusys.backend.model.TeachingAssignment;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.HomeworkAttachmentRepository;
import com.edusys.backend.repository.HomeworkRepository;
import com.edusys.backend.repository.HomeworkSubmissionRepository;
import com.edusys.backend.repository.ParentStudentRepository;
import com.edusys.backend.repository.StudentEnrollmentRepository;
import com.edusys.backend.repository.TeachingAssignmentRepository;
import com.edusys.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class HomeworkService {

    private final HomeworkRepository repo;
    private final TeachingAssignmentRepository teachingAssignmentRepo;
    private final UserRepository userRepo;
    private final StudentAccessService studentAccessService;
    private final ParentStudentRepository parentStudentRepository;
    private final StudentEnrollmentRepository enrollmentRepo;
    private final HomeworkAttachmentRepository homeworkAttachmentRepository;
    private final HomeworkSubmissionRepository homeworkSubmissionRepository;
    private final HomeworkAttachmentService homeworkAttachmentService;

    public HomeworkService(
            HomeworkRepository repo,
            TeachingAssignmentRepository teachingAssignmentRepo,
            UserRepository userRepo,
            StudentAccessService studentAccessService,
            ParentStudentRepository parentStudentRepository,
            StudentEnrollmentRepository enrollmentRepo,
            HomeworkAttachmentRepository homeworkAttachmentRepository,
            HomeworkSubmissionRepository homeworkSubmissionRepository,
            HomeworkAttachmentService homeworkAttachmentService
    ) {
        this.repo = repo;
        this.teachingAssignmentRepo = teachingAssignmentRepo;
        this.userRepo = userRepo;
        this.studentAccessService = studentAccessService;
        this.parentStudentRepository = parentStudentRepository;
        this.enrollmentRepo = enrollmentRepo;
        this.homeworkAttachmentRepository = homeworkAttachmentRepository;
        this.homeworkSubmissionRepository = homeworkSubmissionRepository;
        this.homeworkAttachmentService = homeworkAttachmentService;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private HomeworkResponseDto mapToDto(Homework hw) {
        return mapToDto(hw, null);
    }

    private HomeworkResponseDto mapToDto(Homework hw, HomeworkSubmissionResponseDto submission) {
        List<HomeworkAttachmentDto> attachments = buildAttachmentDtos(hw);
        String attachmentUrl = hw.getAttachmentUrl();
        if ((attachmentUrl == null || attachmentUrl.isBlank()) && !attachments.isEmpty()) {
            attachmentUrl = attachments.getFirst().downloadUrl();
        }

        return new HomeworkResponseDto(
                hw.getId(),
                hw.getTeachingAssignment().getId(),
                hw.getTeachingAssignment().getSubject().getName(),
                hw.getTeachingAssignment().getClassEntity().getClassName(),
                hw.getTeachingAssignment().getTeacher().getFirstName() + " " + hw.getTeachingAssignment().getTeacher().getLastName(),
                hw.getTitle(),
                hw.getDescription(),
                hw.getDueDate(),
                hw.getMaxScore(),
                hw.getType(),
                attachmentUrl,
                attachments,
                hw.getCreatedAt(),
                submission
        );
    }

    public HomeworkResponseDto createHomework(HomeworkCreateDto dto) {
        return createHomework(dto, List.of());
    }

    public HomeworkResponseDto createHomework(HomeworkCreateDto dto, List<MultipartFile> files) {
        User authUser = getCurrentUser();
        if (!authUser.isTeacher()) {
            throw new AccessDeniedException("Only teachers can create homework");
        }

        TeachingAssignment ta = teachingAssignmentRepo.findById(dto.teachingAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found"));

        if (!ta.getTeacher().getId().equals(authUser.getId())) {
            throw new AccessDeniedException("Teacher does not own this teaching assignment");
        }

        Homework hw = new Homework();
        hw.setTeachingAssignment(ta);
        hw.setTitle(dto.title());
        hw.setDescription(dto.description());
        hw.setDueDate(dto.dueDate());
        hw.setMaxScore(dto.maxScore());
        hw.setType(dto.type());
        hw.setAttachmentUrl(normalizeLegacyAttachmentUrl(dto.attachmentUrl()));
        hw.setCreatedAt(LocalDateTime.now());

        Homework saved = repo.save(hw);
        homeworkAttachmentService.storeAttachments(saved, files);
        return mapToDto(saved);
    }

    public HomeworkResponseDto getHomeworkById(Long homeworkId) {
        User authUser = getCurrentUser();
        Homework hw = repo.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        if (!canAccessHomework(hw, authUser)) {
            throw new AccessDeniedException("Access denied");
        }

        return mapToDto(hw);
    }

    public List<HomeworkResponseDto> getHomeworkByTeachingAssignment(Long teachingAssignmentId, User authUser) {
        TeachingAssignment ta = teachingAssignmentRepo.findById(teachingAssignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found"));

        if (!ta.getTeacher().getId().equals(authUser.getId())) {
            throw new AccessDeniedException("Teacher does not own this teaching assignment");
        }

        return repo.findByTeachingAssignmentId(teachingAssignmentId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<HomeworkResponseDto> getHomeworkForCurrentUser(User user) {
        if (user.isAdmin()) {
            return repo.findAll().stream()
                    .map(this::mapToDto)
                    .toList();
        } else if (user.isTeacher()) {
            return getHomeworkForTeacher(user);
        } else if (user.isStudent()) {
            return getHomeworkForCurrentStudent(user);
        } else {
            throw new AccessDeniedException("Unknown role");
        }
    }

    public List<HomeworkResponseDto> getHomeworkForCurrentStudent(User student) {
        if (!student.isStudent()) {
            throw new AccessDeniedException("Only students can view their homework");
        }

        return mapStudentHomeworkDtos(student.getId(), repo.findAllByStudentId(student.getId()));
    }

    public List<HomeworkResponseDto> getHomeworkForStudentId(Long studentId) {
        if (!studentAccessService.canAccessStudent(studentId)) {
            throw new AccessDeniedException("Access denied");
        }

        return mapStudentHomeworkDtos(studentId, repo.findAllByStudentId(studentId));
    }

    private List<HomeworkResponseDto> getHomeworkForTeacher(User teacher) {
        return repo.findByTeachingAssignment_Teacher_Id(teacher.getId())
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public HomeworkResponseDto updateHomework(Long homeworkId, HomeworkCreateDto dto, User authUser) {
        return updateHomework(homeworkId, dto, List.of(), List.of(), authUser);
    }

    public HomeworkResponseDto updateHomework(
            Long homeworkId,
            HomeworkCreateDto dto,
            List<MultipartFile> newFiles,
            List<Long> removeAttachmentIds,
            User authUser
    ) {
        Homework hw = repo.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        if (!hw.getTeachingAssignment().getTeacher().getId().equals(authUser.getId())) {
            throw new AccessDeniedException("Teacher does not own this homework");
        }

        hw.setTitle(dto.title());
        hw.setDescription(dto.description());
        hw.setDueDate(dto.dueDate());
        hw.setMaxScore(dto.maxScore());
        hw.setType(dto.type());
        if (dto.attachmentUrl() != null) {
            hw.setAttachmentUrl(normalizeLegacyAttachmentUrl(dto.attachmentUrl()));
        }

        removeSelectedAttachments(hw, removeAttachmentIds);
        Homework saved = repo.save(hw);
        homeworkAttachmentService.storeAttachments(saved, newFiles);
        return mapToDto(repo.save(saved));
    }

    public void deleteHomework(Long homeworkId, User authUser) {
        Homework hw = repo.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));

        if (!hw.getTeachingAssignment().getTeacher().getId().equals(authUser.getId())) {
            throw new AccessDeniedException("Teacher does not own this homework");
        }

        homeworkAttachmentRepository.findByHomeworkIdOrderByUploadedAtAsc(homeworkId)
                .forEach(homeworkAttachmentService::deleteAttachment);
        homeworkAttachmentService.deleteLegacyAttachment(hw.getAttachmentUrl());
        repo.delete(hw);
    }

    public HomeworkAttachment getAttachmentForHomework(Long homeworkId, Long attachmentId, User authUser) {
        Homework hw = repo.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));
        if (!canAccessHomework(hw, authUser)) {
            throw new AccessDeniedException("Access denied");
        }
        return homeworkAttachmentRepository.findByIdAndHomeworkId(attachmentId, homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework attachment not found"));
    }

    public Homework getAccessibleHomework(Long homeworkId, User authUser) {
        Homework hw = repo.findById(homeworkId)
                .orElseThrow(() -> new ResourceNotFoundException("Homework not found"));
        if (!canAccessHomework(hw, authUser)) {
            throw new AccessDeniedException("Access denied");
        }
        return hw;
    }

    public boolean canAccessHomework(Homework hw, User user) {
        if (user.isAdmin()) {
            return true;
        } else if (user.isTeacher()) {
            return hw.getTeachingAssignment().getTeacher().getId().equals(user.getId());
        } else if (user.isStudent()) {
            return enrollmentRepo.existsByStudent_IdAndClassEntity_Id(
                    user.getId(),
                    hw.getTeachingAssignment().getClassEntity().getId()
            );
        } else if (user.isParent()) {
            Long parentId = user.getId();
            if (parentId == null) return false;

            Long classId = hw.getTeachingAssignment().getClassEntity().getId();
            List<Long> childStudentIds = parentStudentRepository.findStudentIdsByParentId(parentId);
            for (Long childId : childStudentIds) {
                if (childId != null && enrollmentRepo.existsByStudent_IdAndClassEntity_Id(childId, classId)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private List<HomeworkAttachmentDto> buildAttachmentDtos(Homework hw) {
        List<HomeworkAttachmentDto> attachments = new ArrayList<>();
        homeworkAttachmentRepository.findByHomeworkIdOrderByUploadedAtAsc(hw.getId()).stream()
                .sorted(Comparator.comparing(HomeworkAttachment::getUploadedAt))
                .map(this::mapAttachmentToDto)
                .forEach(attachments::add);

        HomeworkAttachmentDto legacyAttachment = mapLegacyAttachmentToDto(hw);
        if (legacyAttachment != null) {
            attachments.add(legacyAttachment);
        }
        return attachments;
    }

    private HomeworkAttachmentDto mapAttachmentToDto(HomeworkAttachment attachment) {
        Long homeworkId = attachment.getHomework().getId();
        boolean previewable = homeworkAttachmentService.isPreviewable(attachment.getMimeType());
        return new HomeworkAttachmentDto(
                attachment.getId(),
                attachment.getOriginalFilename(),
                attachment.getMimeType(),
                attachment.getSizeBytes(),
                attachment.getUploadedAt(),
                "/api/homework/" + homeworkId + "/attachments/" + attachment.getId() + "/download",
                previewable ? "/api/homework/" + homeworkId + "/attachments/" + attachment.getId() + "/preview" : null,
                previewable,
                "stored"
        );
    }

    private HomeworkAttachmentDto mapLegacyAttachmentToDto(Homework hw) {
        String attachmentUrl = normalizeLegacyAttachmentUrl(hw.getAttachmentUrl());
        if (attachmentUrl == null) {
            return null;
        }

        String storedPath = homeworkAttachmentService.extractStoredPath(attachmentUrl);
        String filename = storedPath == null ? "attachment" : storedPath.substring(storedPath.lastIndexOf('/') + 1);
        String mimeType = inferLegacyMimeType(filename);
        boolean previewable = homeworkAttachmentService.isPreviewable(mimeType);

        return new HomeworkAttachmentDto(
                null,
                filename,
                mimeType,
                null,
                hw.getCreatedAt(),
                "/api/homework/" + hw.getId() + "/legacy-attachment/download",
                previewable ? "/api/homework/" + hw.getId() + "/legacy-attachment/preview" : null,
                previewable,
                "legacy"
        );
    }

    private void removeSelectedAttachments(Homework hw, List<Long> removeAttachmentIds) {
        if (removeAttachmentIds == null || removeAttachmentIds.isEmpty()) {
            return;
        }

        List<Long> sanitizedIds = removeAttachmentIds.stream()
                .filter(Objects::nonNull)
                .toList();

        homeworkAttachmentRepository.findByHomeworkIdOrderByUploadedAtAsc(hw.getId()).stream()
                .filter(attachment -> sanitizedIds.contains(attachment.getId()))
                .forEach(homeworkAttachmentService::deleteAttachment);
    }

    private List<HomeworkResponseDto> mapStudentHomeworkDtos(Long studentId, List<Homework> homeworks) {
        List<Long> homeworkIds = homeworks.stream()
                .map(Homework::getId)
                .filter(Objects::nonNull)
                .toList();

        List<HomeworkSubmission> submissions = homeworkIds.isEmpty()
                ? List.of()
                : homeworkSubmissionRepository.findByStudent_IdAndHomework_IdIn(studentId, homeworkIds);

        java.util.Map<Long, HomeworkSubmissionResponseDto> submissionByHomeworkId = submissions.stream()
                .collect(java.util.stream.Collectors.toMap(
                        submission -> submission.getHomework().getId(),
                        this::mapSubmissionToDto,
                        (first, second) -> second
                ));

        return homeworks.stream()
                .map(homework -> mapToDto(homework, submissionByHomeworkId.get(homework.getId())))
                .toList();
    }

    private HomeworkSubmissionResponseDto mapSubmissionToDto(HomeworkSubmission submission) {
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

    private String normalizeLegacyAttachmentUrl(String attachmentUrl) {
        if (attachmentUrl == null || attachmentUrl.isBlank()) {
            return null;
        }
        return attachmentUrl;
    }

    private String inferLegacyMimeType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
}
