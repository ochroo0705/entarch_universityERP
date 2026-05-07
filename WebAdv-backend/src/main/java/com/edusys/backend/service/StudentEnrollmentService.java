package com.edusys.backend.service;

import com.edusys.backend.dto.StudentEnrollmentAssignDTO;
import com.edusys.backend.dto.StudentEnrollmentListItemDTO;
import com.edusys.backend.dto.StudentEnrollmentListQueryDTO;
import com.edusys.backend.dto.StudentEnrollmentResponseDTO;
import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.Class;
import com.edusys.backend.model.StudentEnrollment;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.ClassRepository;
import com.edusys.backend.repository.StudentEnrollmentRepository;
import com.edusys.backend.repository.TeachingAssignmentRepository;
import com.edusys.backend.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class StudentEnrollmentService {

    private final StudentEnrollmentRepository repo;
    private final UserRepository userRepo;
    private final ClassRepository classRepo;
    private final TeachingAssignmentRepository teachingAssignmentRepo;

    public StudentEnrollmentService(
            StudentEnrollmentRepository repo,
            UserRepository userRepo,
            ClassRepository classRepo,
            TeachingAssignmentRepository teachingAssignmentRepo
    ) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.classRepo = classRepo;
        this.teachingAssignmentRepo = teachingAssignmentRepo;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private boolean isTeacher(User user) {
        return user.getRoleFlags() != null && (user.getRoleFlags() & 2) != 0;
    }

    private boolean isStudent(User user) {
        return user.getRoleFlags() != null && (user.getRoleFlags() & 1) != 0;
    }

    private StudentEnrollmentResponseDTO toResponseDTO(StudentEnrollment se) {
        return new StudentEnrollmentResponseDTO(
                se.getId(),
                se.getStudent() == null ? null : se.getStudent().getId(),
                se.getStudent() == null ? null : se.getStudent().getUsername(),
                se.getClassEntity() == null ? null : se.getClassEntity().getId(),
                se.getClassEntity() == null ? null : se.getClassEntity().getClassName(),
                se.getEnrollmentDate(),
                se.getStudentNumber(),
                se.getStatus()
        );
    }

    public StudentEnrollment save(StudentEnrollment e) {
        return repo.save(e);
    }

    @Transactional
    public StudentEnrollmentResponseDTO createEnrollmentAsAdmin(StudentEnrollmentAssignDTO dto) {
        User authUser = getCurrentUser();
        if (authUser.getRoleFlags() == null || (authUser.getRoleFlags() & 8) == 0) {
            throw new AccessDeniedException("Only admins can create enrollments");
        }

        Class classEntity = classRepo.findById(dto.classId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        User student = userRepo.findById(dto.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!isStudent(student)) {
            throw new IllegalArgumentException(
                    "User " + dto.studentId() + " is not a STUDENT (roleFlags=" + student.getRoleFlags() + ")"
            );
        }

        if (repo.existsByStudent_IdAndClassEntity_Id(student.getId(), classEntity.getId())) {
            throw new IllegalStateException("Student is already enrolled in this class");
        }

        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setStudent(student);
        enrollment.setClassEntity(classEntity);
        enrollment.setEnrollmentDate(dto.enrollmentDate() == null ? LocalDate.now() : dto.enrollmentDate());
        enrollment.setStudentNumber(dto.studentNumber());
        enrollment.setStatus(dto.status() == null ? StudentEnrollment.Status.active : dto.status());

        StudentEnrollment saved = repo.save(enrollment);
        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDTO<StudentEnrollmentListItemDTO> listEnrollments(StudentEnrollmentListQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        Page<StudentEnrollment> enrollmentPage = repo.findAll(
                buildListSpecification(query),
                PageRequest.of(page - 1, pageSize, buildListSort(query.getSortBy(), query.getSortOrder()))
        );

        List<StudentEnrollmentListItemDTO> items = enrollmentPage.getContent().stream()
                .map(this::toListItemDTO)
                .toList();

        return new PaginatedResponseDTO<>(
                items,
                enrollmentPage.getNumber() + 1,
                enrollmentPage.getSize(),
                enrollmentPage.getTotalElements(),
                enrollmentPage.getTotalPages()
        );
    }

    /**
     * Teacher enrolls a student into a class, only if the teacher has an active teaching assignment for that class.
     */
    @Transactional
    public StudentEnrollmentResponseDTO enrollStudentAsTeacher(StudentEnrollmentAssignDTO dto) {
        User authUser = getCurrentUser();
        if (!isTeacher(authUser)) {
            throw new AccessDeniedException("Only teachers can enroll students");
        }

        Class classEntity = classRepo.findById(dto.classId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        boolean teachesThisClass = teachingAssignmentRepo.existsByTeacher_IdAndClassEntity_IdAndIsActiveTrue(
                authUser.getId(),
                classEntity.getId()
        );
        if (!teachesThisClass) {
            throw new AccessDeniedException("Teacher is not assigned to this class");
        }

        User student = userRepo.findById(dto.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (!isStudent(student)) {
            throw new IllegalArgumentException(
                    "User " + dto.studentId() + " is not a STUDENT (roleFlags=" + student.getRoleFlags() + ")"
            );
        }

        if (repo.existsByStudent_IdAndClassEntity_Id(student.getId(), classEntity.getId())) {
            throw new IllegalStateException("Student is already enrolled in this class");
        }

        StudentEnrollment se = new StudentEnrollment();
        se.setStudent(student);
        se.setClassEntity(classEntity);
        se.setEnrollmentDate(dto.enrollmentDate() == null ? LocalDate.now() : dto.enrollmentDate());
        se.setStudentNumber(dto.studentNumber());
        se.setStatus(dto.status() == null ? StudentEnrollment.Status.active : dto.status());

        StudentEnrollment saved = repo.save(se);
        return toResponseDTO(saved);
    }

    /**
     * Teacher removes a student from a class, only if the teacher has an active teaching assignment for that class.
     */
    @Transactional
    public void unenrollStudentAsTeacher(StudentEnrollmentAssignDTO dto) {
        User authUser = getCurrentUser();
        if (!isTeacher(authUser)) {
            throw new AccessDeniedException("Only teachers can unenroll students");
        }

        Class classEntity = classRepo.findById(dto.classId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        boolean teachesThisClass = teachingAssignmentRepo.existsByTeacher_IdAndClassEntity_IdAndIsActiveTrue(
                authUser.getId(),
                classEntity.getId()
        );
        if (!teachesThisClass) {
            throw new AccessDeniedException("Teacher is not assigned to this class");
        }

        User student = userRepo.findById(dto.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        StudentEnrollment enrollment = repo.findByStudent_IdAndClassEntity_Id(student.getId(), classEntity.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        repo.delete(enrollment);
    }

    public Optional<StudentEnrollment> findById(Long id) {
        return repo.findById(id);
    }

    public List<StudentEnrollment> findAll() {
        return repo.findAll();
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private StudentEnrollmentListItemDTO toListItemDTO(StudentEnrollment enrollment) {
        User student = enrollment.getStudent();
        Class classEntity = enrollment.getClassEntity();

        return new StudentEnrollmentListItemDTO(
                enrollment.getId(),
                student == null ? null : new StudentEnrollmentListItemDTO.StudentDTO(
                        student.getId(),
                        student.getUsername(),
                        student.getFirstName(),
                        student.getLastName()
                ),
                classEntity == null ? null : new StudentEnrollmentListItemDTO.ClassDTO(
                        classEntity.getId(),
                        classEntity.getClassName(),
                        classEntity.getGrade(),
                        classEntity.getSection()
                ),
                enrollment.getEnrollmentDate(),
                enrollment.getStudentNumber(),
                enrollment.getStatus()
        );
    }

    private Sort buildListSort(String sortBy, String sortOrder) {
        String requestedSort = sortBy == null || sortBy.isBlank() ? "enrollmentDate" : sortBy.trim();
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (requestedSort) {
            case "id" -> Sort.by(direction, "id");
            case "enrollmentDate" -> Sort.by(direction, "enrollmentDate").and(Sort.by(direction, "id"));
            case "studentName" -> Sort.by(direction, "student.lastName", "student.firstName", "id");
            case "studentNumber" -> Sort.by(direction, "studentNumber").and(Sort.by(direction, "id"));
            case "className" -> Sort.by(direction, "classEntity.className").and(Sort.by(direction, "id"));
            case "status" -> Sort.by(direction, "status").and(Sort.by(direction, "id"));
            default -> throw new IllegalArgumentException("Unsupported sortBy value");
        };
    }

    private Specification<StudentEnrollment> buildListSpecification(StudentEnrollmentListQueryDTO query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Join<StudentEnrollment, User> studentJoin = root.join("student", JoinType.LEFT);
            Join<StudentEnrollment, Class> classJoin = root.join("classEntity", JoinType.LEFT);

            criteriaQuery.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (query.getSearch() != null && !query.getSearch().isBlank()) {
                String term = "%" + query.getSearch().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(studentJoin.get("firstName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(studentJoin.get("lastName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(studentJoin.get("username")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(classJoin.get("className")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("studentNumber")), term)
                ));
            }

            if (query.getStatus() != null && !query.getStatus().isBlank()) {
                Set<StudentEnrollment.Status> statuses = resolveStatuses(query.getStatus());
                if (statuses.isEmpty()) {
                    throw new IllegalArgumentException("Unsupported status filter");
                }
                predicates.add(root.get("status").in(statuses));
            }

            if (query.getClassId() != null) {
                predicates.add(criteriaBuilder.equal(classJoin.get("id"), query.getClassId()));
            }
            if (query.getGrade() != null) {
                predicates.add(criteriaBuilder.equal(classJoin.get("grade"), query.getGrade()));
            }
            if (query.getSection() != null && !query.getSection().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(classJoin.get("section")),
                        query.getSection().trim().toLowerCase(Locale.ROOT)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Set<StudentEnrollment.Status> resolveStatuses(String rawStatus) {
        String normalized = rawStatus.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ACTIVE" -> Set.of(StudentEnrollment.Status.ACTIVE, StudentEnrollment.Status.active);
            case "GRADUATED" -> Set.of(StudentEnrollment.Status.GRADUATED);
            case "TRANSFERRED" -> Set.of(StudentEnrollment.Status.TRANSFERRED);
            case "DROPPED" -> Set.of(StudentEnrollment.Status.DROPPED);
            default -> Set.of();
        };
    }
}
