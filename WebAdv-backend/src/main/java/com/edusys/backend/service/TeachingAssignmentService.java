package com.edusys.backend.service;

import com.edusys.backend.dto.TeachingAssignmentAssignDTO;
import com.edusys.backend.dto.TeachingAssignmentClassDetailDTO;
import com.edusys.backend.dto.TeachingAssignmentDetailDTO;
import com.edusys.backend.dto.TeachingAssignmentGroupDTO;
import com.edusys.backend.dto.TeachingAssignmentListItemDTO;
import com.edusys.backend.dto.TeachingAssignmentListQueryDTO;
import com.edusys.backend.dto.TeachingAssignmentResponseDTO;
import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.Class;
import com.edusys.backend.model.Subject;
import com.edusys.backend.model.TeachingAssignment;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.ClassRepository;
import com.edusys.backend.repository.SubjectRepository;
import com.edusys.backend.repository.TeachingAssignmentRepository;
import com.edusys.backend.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class TeachingAssignmentService {

    private final TeachingAssignmentRepository repo;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final ClassRepository classRepository;

    public TeachingAssignmentService(
            TeachingAssignmentRepository repo,
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            ClassRepository classRepository
    ) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.classRepository = classRepository;
    }

    public TeachingAssignment save(TeachingAssignment t) {
        return repo.save(t);
    }

    public Optional<TeachingAssignment> findById(Long id) {
        return repo.findById(id);
    }

    public List<TeachingAssignment> findAll() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDTO<TeachingAssignmentListItemDTO> listAssignments(TeachingAssignmentListQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        Page<TeachingAssignment> assignmentPage = repo.findAll(
                buildListSpecification(query),
                PageRequest.of(page - 1, pageSize, buildListSort(query.getSortBy(), query.getSortOrder()))
        );

        List<TeachingAssignmentListItemDTO> items = assignmentPage.getContent().stream()
                .map(this::toListItemDTO)
                .toList();

        return new PaginatedResponseDTO<>(
                items,
                assignmentPage.getNumber() + 1,
                assignmentPage.getSize(),
                assignmentPage.getTotalElements(),
                assignmentPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public List<TeachingAssignmentDetailDTO> getMyTeachingAssignments(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResourceNotFoundException("Unauthenticated request");
        }

        String principal = authentication.getName();
        User teacher = userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .or(() -> userRepository.findByPhone(principal))
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found for authenticated user"));

        List<TeachingAssignment> assignments = repo.findAllDetailedByTeacher_IdAndIsActiveTrue(teacher.getId());
        return assignments.stream().map(this::toDetailDTO).toList();
    }

        @Transactional(readOnly = true)
        public TeachingAssignmentGroupDTO getGroup(Long groupId) {
        TeachingAssignment reference = repo.findDetailedById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found"));

        List<TeachingAssignment> groupAssignments = repo.findAllDetailedByTeacher_IdAndSubject_IdAndAcademicYearAndSemesterAndIsActiveTrue(
            reference.getTeacher() == null ? null : reference.getTeacher().getId(),
            reference.getSubject() == null ? null : reference.getSubject().getId(),
            reference.getAcademicYear(),
            reference.getSemester()
        );

        if (groupAssignments.isEmpty()) {
            // If the reference TA is inactive, the above query may return empty.
            // Fall back to returning just the reference details.
            groupAssignments = List.of(reference);
        }

        TeachingAssignmentDetailDTO.TeacherDTO teacherDto = toTeacherDTO(reference.getTeacher());
        TeachingAssignmentDetailDTO.SubjectDTO subjectDto = toSubjectDTO(reference.getSubject());

        TeachingAssignmentGroupDTO.TeacherDTO groupTeacher = teacherDto == null ? null
            : new TeachingAssignmentGroupDTO.TeacherDTO(
            teacherDto.id(), teacherDto.username(), teacherDto.firstName(), teacherDto.lastName()
        );

        TeachingAssignmentGroupDTO.SubjectDTO groupSubject = subjectDto == null ? null
            : new TeachingAssignmentGroupDTO.SubjectDTO(
            subjectDto.id(), subjectDto.name(), subjectDto.subjectCode(), subjectDto.gradeLevel(), subjectDto.hoursPerWeek()
        );

        List<TeachingAssignmentGroupDTO.ClassSummaryDTO> classes = groupAssignments.stream()
            .filter(ta -> ta.getClassEntity() != null)
            .map(ta -> new TeachingAssignmentGroupDTO.ClassSummaryDTO(
                ta.getId(),
                ta.getClassEntity().getId(),
                ta.getClassEntity().getClassName(),
                ta.getClassEntity().getGrade(),
                ta.getClassEntity().getSection(),
                ta.getClassEntity().getRoomNumber(),
                ta.getClassEntity().getAcademicYear()
            ))
            .toList();

        return new TeachingAssignmentGroupDTO(
            reference.getId(),
            groupTeacher,
            groupSubject,
            reference.getAcademicYear(),
            reference.getSemester(),
            reference.getIsActive(),
            classes
        );
        }

        @Transactional(readOnly = true)
        public TeachingAssignmentClassDetailDTO getClassDetails(Long groupId, Long classId) {
        TeachingAssignment reference = repo.findDetailedById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found"));

        Long teacherId = reference.getTeacher() == null ? null : reference.getTeacher().getId();
        Long subjectId = reference.getSubject() == null ? null : reference.getSubject().getId();
        if (teacherId == null || subjectId == null) {
            throw new ResourceNotFoundException("Teaching assignment is missing teacher or subject");
        }

        TeachingAssignment ta = repo.findDetailedByTeacher_IdAndSubject_IdAndAcademicYearAndSemesterAndIsActiveTrueAndClassEntity_Id(
                teacherId,
                subjectId,
                reference.getAcademicYear(),
                reference.getSemester(),
                classId
            )
            .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found for this class"));

        TeachingAssignmentDetailDTO detail = toDetailDTO(ta);
        return new TeachingAssignmentClassDetailDTO(
            groupId,
            ta.getId(),
            detail.teacher(),
            detail.subject(),
            detail.classInfo(),
            detail.academicYear(),
            detail.semester(),
            detail.isActive()
        );
        }

    @Transactional(readOnly = true)
    public TeachingAssignmentDetailDTO getDetails(Long id) {
        TeachingAssignment ta = repo.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found"));
        return toDetailDTO(ta);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Transactional
    public TeachingAssignment assignTeacherToSubject(@Valid TeachingAssignmentAssignDTO dto) {
        User teacher = userRepository.findById(dto.teacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        if (!teacher.isTeacher()) {
            throw new IllegalArgumentException(
                "User " + dto.teacherId() + " is not a TEACHER (roleFlags=" + teacher.getRoleFlags() + ")"
            );
        }

        Subject subject = subjectRepository.findById(dto.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        Class classEntity = classRepository.findById(dto.classId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        if (repo.existsByTeacher_IdAndSubject_IdAndClassEntity_IdAndAcademicYearAndSemesterAndIsActiveTrue(
                teacher.getId(),
                subject.getId(),
                classEntity.getId(),
                dto.academicYear(),
                dto.semester()
        )) {
            throw new IllegalStateException("Active teaching assignment already exists for this teacher/subject/class/year/semester");
        }

        TeachingAssignment ta = new TeachingAssignment();
        ta.setTeacher(teacher);
        ta.setSubject(subject);
        ta.setClassEntity(classEntity);
        ta.setAcademicYear(dto.academicYear());
        ta.setSemester(dto.semester());
        ta.setIsActive(dto.isActive() == null ? true : dto.isActive());

        return repo.save(ta);
    }

    @Transactional
    public TeachingAssignment updateTeachingAssignment(Long id, @Valid TeachingAssignmentAssignDTO dto) {
        TeachingAssignment existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found"));

        User teacher = userRepository.findById(dto.teacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        if (!teacher.isTeacher()) {
            throw new IllegalArgumentException(
                    "User " + dto.teacherId() + " is not a TEACHER (roleFlags=" + teacher.getRoleFlags() + ")"
            );
        }

        Subject subject = subjectRepository.findById(dto.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        Class classEntity = classRepository.findById(dto.classId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        boolean willBeActive = dto.isActive() == null ? (existing.getIsActive() != null && existing.getIsActive()) : dto.isActive();

        if (willBeActive && repo.existsByTeacher_IdAndSubject_IdAndClassEntity_IdAndAcademicYearAndSemesterAndIsActiveTrueAndIdNot(
                teacher.getId(),
                subject.getId(),
                classEntity.getId(),
                dto.academicYear(),
                dto.semester(),
                existing.getId()
        )) {
            throw new IllegalStateException("Active teaching assignment already exists for this teacher/subject/class/year/semester");
        }

        existing.setTeacher(teacher);
        existing.setSubject(subject);
        existing.setClassEntity(classEntity);
        existing.setAcademicYear(dto.academicYear());
        existing.setSemester(dto.semester());
        existing.setIsActive(dto.isActive() == null ? existing.getIsActive() : dto.isActive());

        return repo.save(existing);
    }

    @Transactional
    public void deactivateTeachingAssignment(Long id) {
        TeachingAssignment existing = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found"));
        existing.setIsActive(false);
        repo.save(existing);
    }

    public TeachingAssignmentResponseDTO toResponseDTO(TeachingAssignment ta) {
        return new TeachingAssignmentResponseDTO(
                ta.getId(),
                ta.getTeacher() == null ? null : ta.getTeacher().getId(),
                ta.getTeacher() == null ? null : ta.getTeacher().getUsername(),
                ta.getSubject() == null ? null : ta.getSubject().getId(),
                ta.getSubject() == null ? null : ta.getSubject().getName(),
                ta.getClassEntity() == null ? null : ta.getClassEntity().getId(),
                ta.getClassEntity() == null ? null : ta.getClassEntity().getClassName(),
                ta.getAcademicYear(),
                ta.getSemester(),
                ta.getIsActive()
        );
    }

    public TeachingAssignmentListItemDTO toListItemDTO(TeachingAssignment ta) {
        User teacher = ta.getTeacher();
        Subject subject = ta.getSubject();
        Class classEntity = ta.getClassEntity();

        return new TeachingAssignmentListItemDTO(
                ta.getId(),
                teacher == null ? null : new TeachingAssignmentListItemDTO.TeacherDTO(
                        teacher.getId(),
                        teacher.getUsername(),
                        teacher.getFirstName(),
                        teacher.getLastName()
                ),
                subject == null ? null : new TeachingAssignmentListItemDTO.SubjectDTO(
                        subject.getId(),
                        subject.getName(),
                        subject.getSubjectNameMn(),
                        subject.getSubjectCode(),
                        subject.getGradeLevel(),
                        subject.getHoursPerWeek()
                ),
                classEntity == null ? null : new TeachingAssignmentListItemDTO.ClassInfoDTO(
                        classEntity.getId(),
                        classEntity.getClassName(),
                        classEntity.getGrade(),
                        classEntity.getSection(),
                        classEntity.getRoomNumber(),
                        classEntity.getAcademicYear()
                ),
                ta.getAcademicYear(),
                ta.getSemester(),
                ta.getIsActive()
        );
    }

    private TeachingAssignmentDetailDTO.TeacherDTO toTeacherDTO(User teacher) {
        return teacher == null
                ? null
                : new TeachingAssignmentDetailDTO.TeacherDTO(
                teacher.getId(),
                teacher.getUsername(),
                teacher.getFirstName(),
                teacher.getLastName()
        );
    }

    private TeachingAssignmentDetailDTO.SubjectDTO toSubjectDTO(Subject subject) {
        return subject == null
                ? null
                : new TeachingAssignmentDetailDTO.SubjectDTO(
                subject.getId(),
                subject.getName(),
                subject.getSubjectNameMn(),
                subject.getSubjectCode(),
                subject.getGradeLevel(),
                subject.getHoursPerWeek()
        );
    }

    public TeachingAssignmentDetailDTO toDetailDTO(TeachingAssignment ta) {
        User teacher = ta.getTeacher();
        Subject subject = ta.getSubject();
        Class classEntity = ta.getClassEntity();

        TeachingAssignmentDetailDTO.TeacherDTO teacherDto = toTeacherDTO(teacher);
        TeachingAssignmentDetailDTO.SubjectDTO subjectDto = toSubjectDTO(subject);

        TeachingAssignmentDetailDTO.TeacherDTO homeroomTeacherDto = null;
        java.util.List<TeachingAssignmentDetailDTO.TeacherDTO> assistantTeacherDtos = java.util.List.of();
        if (classEntity != null) {
            if (classEntity.getHomeroomTeacher() != null) {
            User ht = classEntity.getHomeroomTeacher();
            homeroomTeacherDto = new TeachingAssignmentDetailDTO.TeacherDTO(
                ht.getId(),
                ht.getUsername(),
                ht.getFirstName(),
                ht.getLastName()
            );
            }

            if (classEntity.getAssistantTeachers() != null) {
            assistantTeacherDtos = classEntity.getAssistantTeachers().stream()
                .map(t -> new TeachingAssignmentDetailDTO.TeacherDTO(
                    t.getId(),
                    t.getUsername(),
                    t.getFirstName(),
                    t.getLastName()
                ))
                .toList();
            }
        }

        TeachingAssignmentDetailDTO.ClassInfoDTO classDto = classEntity == null
            ? null
            : new TeachingAssignmentDetailDTO.ClassInfoDTO(
            classEntity.getId(),
            classEntity.getClassName(),
            classEntity.getGrade(),
            classEntity.getSection(),
            classEntity.getRoomNumber(),
            classEntity.getAcademicYear(),
            homeroomTeacherDto,
            assistantTeacherDtos
        );

        return new TeachingAssignmentDetailDTO(
            ta.getId(),
            teacherDto,
            subjectDto,
            classDto,
            ta.getAcademicYear(),
            ta.getSemester(),
            ta.getIsActive()
        );
    }

    private Sort buildListSort(String sortBy, String sortOrder) {
        String requestedSort = sortBy == null || sortBy.isBlank() ? "id" : sortBy.trim();
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (requestedSort) {
            case "id" -> Sort.by(direction, "id");
            case "teacher" -> Sort.by(direction, "teacher.lastName", "teacher.firstName", "id");
            case "subject" -> Sort.by(direction, "subject.subjectName", "subject.subjectCode", "id");
            case "className" -> Sort.by(direction, "classEntity.className", "id");
            case "academicYear" -> Sort.by(direction, "academicYear").and(Sort.by(direction, "id"));
            case "semester" -> Sort.by(direction, "semester").and(Sort.by(direction, "id"));
            case "isActive" -> Sort.by(direction, "isActive").and(Sort.by(direction, "id"));
            default -> throw new IllegalArgumentException("Unsupported sortBy value");
        };
    }

    private Specification<TeachingAssignment> buildListSpecification(TeachingAssignmentListQueryDTO query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Join<TeachingAssignment, User> teacherJoin = root.join("teacher", JoinType.LEFT);
            Join<TeachingAssignment, Subject> subjectJoin = root.join("subject", JoinType.LEFT);
            Join<TeachingAssignment, Class> classJoin = root.join("classEntity", JoinType.LEFT);

            criteriaQuery.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (query.getSearch() != null && !query.getSearch().isBlank()) {
                String term = "%" + query.getSearch().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("firstName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("lastName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(teacherJoin.get("username")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(subjectJoin.get("subjectName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(subjectJoin.get("subjectCode")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(classJoin.get("className")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("academicYear")), term)
                ));
            }

            if (query.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), query.getStatus()));
            }
            if (query.getTeacherId() != null) {
                predicates.add(criteriaBuilder.equal(teacherJoin.get("id"), query.getTeacherId()));
            }
            if (query.getSubjectId() != null) {
                predicates.add(criteriaBuilder.equal(subjectJoin.get("id"), query.getSubjectId()));
            }
            if (query.getClassId() != null) {
                predicates.add(criteriaBuilder.equal(classJoin.get("id"), query.getClassId()));
            }
            if (query.getSemester() != null) {
                predicates.add(criteriaBuilder.equal(root.get("semester"), query.getSemester()));
            }
            if (query.getAcademicYear() != null && !query.getAcademicYear().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("academicYear")),
                        query.getAcademicYear().trim().toLowerCase(Locale.ROOT)
                ));
            }
            if (query.getGrade() != null) {
                predicates.add(criteriaBuilder.equal(classJoin.get("grade"), query.getGrade()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
