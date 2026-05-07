package com.edusys.backend.service;

import com.edusys.backend.dto.ExamScheduleCreateDTO;
import com.edusys.backend.dto.ExamScheduleListQueryDTO;
import com.edusys.backend.dto.ExamScheduleResponseDTO;
import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.Class;
import com.edusys.backend.model.ExamSchedule;
import com.edusys.backend.model.TeachingAssignment;
import com.edusys.backend.repository.ExamScheduleRepository;
import com.edusys.backend.repository.TeachingAssignmentRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ExamScheduleService {

    private final ExamScheduleRepository examScheduleRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;

    public ExamScheduleService(
            ExamScheduleRepository examScheduleRepository,
            TeachingAssignmentRepository teachingAssignmentRepository
    ) {
        this.examScheduleRepository = examScheduleRepository;
        this.teachingAssignmentRepository = teachingAssignmentRepository;
    }

    @Transactional
    public ExamScheduleResponseDTO createExamSchedule(ExamScheduleCreateDTO dto) {
        validateTimes(dto);
        TeachingAssignment teachingAssignment = getTeachingAssignment(dto.teachingAssignmentId());
        ensureNoClassConflict(teachingAssignment, dto, null);

        ExamSchedule examSchedule = new ExamSchedule();
        apply(examSchedule, teachingAssignment, dto);
        return mapToResponseDTO(examScheduleRepository.save(examSchedule));
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDTO<ExamScheduleResponseDTO> listExamSchedules(ExamScheduleListQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        Page<ExamSchedule> examPage = examScheduleRepository.findAll(
                buildListSpecification(query),
                PageRequest.of(page - 1, pageSize, buildListSort(query.getSortBy(), query.getSortOrder()))
        );

        List<ExamScheduleResponseDTO> items = examPage.getContent().stream()
                .map(this::mapToResponseDTO)
                .toList();

        return new PaginatedResponseDTO<>(
                items,
                examPage.getNumber() + 1,
                examPage.getSize(),
                examPage.getTotalElements(),
                examPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public ExamScheduleResponseDTO getExamScheduleById(Long id) {
        return mapToResponseDTO(getExamSchedule(id));
    }

    @Transactional
    public ExamScheduleResponseDTO updateExamSchedule(Long id, ExamScheduleCreateDTO dto) {
        validateTimes(dto);
        ExamSchedule examSchedule = getExamSchedule(id);
        TeachingAssignment teachingAssignment = getTeachingAssignment(dto.teachingAssignmentId());
        ensureNoClassConflict(teachingAssignment, dto, id);

        apply(examSchedule, teachingAssignment, dto);
        return mapToResponseDTO(examScheduleRepository.save(examSchedule));
    }

    @Transactional
    public void deleteExamSchedule(Long id) {
        ExamSchedule examSchedule = getExamSchedule(id);
        examSchedule.setIsActive(false);
        examScheduleRepository.save(examSchedule);
    }

    @Transactional(readOnly = true)
    public List<ExamScheduleResponseDTO> getTeacherExamSchedules(Long teacherId) {
        return examScheduleRepository.findPublishedByTeacherId(teacherId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExamScheduleResponseDTO> getStudentExamSchedules(Long studentId) {
        return examScheduleRepository.findPublishedByStudentId(studentId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private void apply(ExamSchedule examSchedule, TeachingAssignment teachingAssignment, ExamScheduleCreateDTO dto) {
        examSchedule.setTeachingAssignment(teachingAssignment);
        examSchedule.setExamDate(dto.examDate());
        examSchedule.setStartTime(dto.startTime());
        examSchedule.setEndTime(dto.endTime());
        examSchedule.setRoomNumber(dto.roomNumber());
        examSchedule.setTitle(dto.title().trim());
        examSchedule.setNotes(dto.notes() == null || dto.notes().isBlank() ? null : dto.notes().trim());
        examSchedule.setPublished(Boolean.TRUE.equals(dto.published()));
        if (examSchedule.getIsActive() == null) {
            examSchedule.setIsActive(true);
        }
    }

    private void validateTimes(ExamScheduleCreateDTO dto) {
        if (!dto.endTime().isAfter(dto.startTime())) {
            throw new IllegalArgumentException("endTime must be later than startTime");
        }
    }

    private TeachingAssignment getTeachingAssignment(Long teachingAssignmentId) {
        TeachingAssignment teachingAssignment = teachingAssignmentRepository.findById(teachingAssignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found"));

        if (teachingAssignment.getClassEntity() == null || teachingAssignment.getTeacher() == null || teachingAssignment.getSubject() == null) {
            throw new IllegalArgumentException("Teaching assignment must have class, teacher, and subject");
        }
        return teachingAssignment;
    }

    private ExamSchedule getExamSchedule(Long id) {
        return examScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam schedule not found"));
    }

    private void ensureNoClassConflict(TeachingAssignment teachingAssignment, ExamScheduleCreateDTO dto, Long excludeId) {
        List<ExamSchedule> conflicts = examScheduleRepository.findClassConflicts(
                teachingAssignment.getClassEntity().getId(),
                dto.examDate(),
                dto.startTime(),
                dto.endTime(),
                excludeId
        );

        if (!conflicts.isEmpty()) {
            ExamSchedule existing = conflicts.get(0);
            throw new IllegalArgumentException(
                    "Class conflict: " + teachingAssignment.getClassEntity().getClassName()
                            + " already has an exam scheduled on " + existing.getExamDate()
                            + " from " + existing.getStartTime() + " to " + existing.getEndTime()
            );
        }
    }

    private ExamScheduleResponseDTO mapToResponseDTO(ExamSchedule examSchedule) {
        TeachingAssignment assignment = examSchedule.getTeachingAssignment();
        return new ExamScheduleResponseDTO(
                examSchedule.getId(),
                assignment.getId(),
                assignment.getTeacher().getId(),
                assignment.getClassEntity().getId(),
                assignment.getSubject().getId(),
                examSchedule.getExamDate().toString(),
                examSchedule.getStartTime().toString(),
                examSchedule.getEndTime().toString(),
                examSchedule.getRoomNumber(),
                examSchedule.getTitle(),
                examSchedule.getNotes(),
                assignment.getSubject().getName(),
                assignment.getTeacher().getFirstName() + " " + assignment.getTeacher().getLastName(),
                assignment.getClassEntity().getClassName(),
                assignment.getClassEntity().getGrade(),
                examSchedule.getPublished(),
                examSchedule.getIsActive(),
                examSchedule.getCreatedAt() == null ? null : examSchedule.getCreatedAt().toString(),
                examSchedule.getUpdatedAt() == null ? null : examSchedule.getUpdatedAt().toString()
        );
    }

    private Sort buildListSort(String sortBy, String sortOrder) {
        String requestedSort = sortBy == null || sortBy.isBlank() ? "examDate" : sortBy.trim();
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (requestedSort) {
            case "id" -> Sort.by(direction, "id");
            case "examDate" -> Sort.by(direction, "examDate", "startTime", "id");
            case "startTime" -> Sort.by(direction, "startTime", "examDate", "id");
            case "title" -> Sort.by(direction, "title").and(Sort.by(direction, "id"));
            case "subject" -> Sort.by(direction, "teachingAssignment.subject.subjectName").and(Sort.by(direction, "id"));
            case "teacher" -> Sort.by(direction, "teachingAssignment.teacher.lastName", "teachingAssignment.teacher.firstName", "id");
            case "className" -> Sort.by(direction, "teachingAssignment.classEntity.className").and(Sort.by(direction, "id"));
            case "roomNumber" -> Sort.by(direction, "roomNumber").and(Sort.by(direction, "id"));
            case "published" -> Sort.by(direction, "published").and(Sort.by(Sort.Direction.ASC, "examDate", "startTime", "id"));
            default -> throw new IllegalArgumentException("Unsupported sortBy value");
        };
    }

    private Specification<ExamSchedule> buildListSpecification(ExamScheduleListQueryDTO query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Join<ExamSchedule, TeachingAssignment> assignmentJoin = root.join("teachingAssignment", JoinType.LEFT);
            Join<TeachingAssignment, com.edusys.backend.model.User> teacherJoin = assignmentJoin.join("teacher", JoinType.LEFT);
            Join<TeachingAssignment, com.edusys.backend.model.Subject> subjectJoin = assignmentJoin.join("subject", JoinType.LEFT);
            Join<TeachingAssignment, Class> classJoin = assignmentJoin.join("classEntity", JoinType.LEFT);

            criteriaQuery.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (query.getSearch() != null && !query.getSearch().isBlank()) {
                String term = "%" + query.getSearch().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("notes")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(subjectJoin.get("subjectName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("firstName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("lastName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(classJoin.get("className")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("roomNumber")), term)
                ));
            }

            if (query.getExamDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("examDate"), query.getExamDate()));
            }
            if (query.getClassId() != null) {
                predicates.add(criteriaBuilder.equal(classJoin.get("id"), query.getClassId()));
            }
            if (query.getTeacherId() != null) {
                predicates.add(criteriaBuilder.equal(teacherJoin.get("id"), query.getTeacherId()));
            }
            if (query.getSubjectId() != null) {
                predicates.add(criteriaBuilder.equal(subjectJoin.get("id"), query.getSubjectId()));
            }
            if (query.getPublished() != null) {
                predicates.add(criteriaBuilder.equal(root.get("published"), query.getPublished()));
            }
            if (query.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), query.getIsActive()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
