package com.edusys.backend.service;

import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.dto.RoleAssignmentRequestDTO;
import com.edusys.backend.dto.RoleOptionDTO;
import com.edusys.backend.dto.UserClassSummaryDTO;
import com.edusys.backend.dto.UserListItemDTO;
import com.edusys.backend.dto.UserListQueryDTO;
import com.edusys.backend.model.Class;
import com.edusys.backend.model.StudentEnrollment;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.StudentEnrollmentRepository;
import com.edusys.backend.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private static final List<RoleOptionDTO> ROLE_OPTIONS = List.of(
            new RoleOptionDTO(User.ROLE_STUDENT, "STUDENT", "Student"),
            new RoleOptionDTO(User.ROLE_TEACHER, "TEACHER", "Teacher"),
            new RoleOptionDTO(User.ROLE_PARENT, "PARENT", "Parent"),
            new RoleOptionDTO(User.ROLE_ADMIN, "ADMIN", "Admin"),
            new RoleOptionDTO(User.ROLE_COUNSELOR, "COUNSELOR", "Counselor"),
            new RoleOptionDTO(User.ROLE_NURSE, "NURSE", "Nurse"),
            new RoleOptionDTO(User.ROLE_FINANCE_STAFF, "FINANCE_STAFF", "Finance staff"),
            new RoleOptionDTO(User.ROLE_LIBRARIAN, "LIBRARIAN", "Librarian"),
            new RoleOptionDTO(User.ROLE_TRANSPORT_COORDINATOR, "TRANSPORT_COORDINATOR", "Transport coordinator"),
            new RoleOptionDTO(User.ROLE_ADMISSIONS_STAFF, "ADMISSIONS_STAFF", "Admissions staff"),
            new RoleOptionDTO(User.ROLE_CAFETERIA_STAFF, "CAFETERIA_STAFF", "Cafeteria staff")
    );
    private static final int ALL_ROLE_FLAGS = ROLE_OPTIONS.stream()
            .mapToInt(RoleOptionDTO::flag)
            .reduce(0, (left, right) -> left | right);

    private final UserRepository userRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            StudentEnrollmentRepository studentEnrollmentRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private void copyOptionalProfileFields(User source, User target) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setPhone(source.getPhone());
        target.setAddress(source.getAddress());
        target.setDateOfBirth(source.getDateOfBirth());
        target.setGender(source.getGender());
        target.setProfilePicture(source.getProfilePicture());
        if (source.getIsActive() != null) {
            target.setIsActive(source.getIsActive());
        }
    }

    public User createUser(User user) {
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException("Password is required (send JSON field 'password')");
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<RoleOptionDTO> getRoleOptions() {
        return ROLE_OPTIONS;
    }

    public User updateUserRoles(Long userId, RoleAssignmentRequestDTO request) {
        int roleFlags = request.roleFlags();
        validateRoleFlags(roleFlags);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRoleFlags(roleFlags);
        return userRepository.save(user);
    }

    public PaginatedResponseDTO<UserListItemDTO> listUsers(UserListQueryDTO query) {
        validateRole(query.getRole());

        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        Page<User> userPage = userRepository.findAll(
                buildSpecification(query),
                PageRequest.of(page - 1, pageSize, buildSort(query.getSortBy(), query.getSortOrder()))
        );

        Map<Long, UserClassSummaryDTO> classSummaries = loadClassSummaries(
                userPage.getContent().stream().map(User::getId).toList()
        );

        List<UserListItemDTO> items = userPage.getContent().stream()
                .map(user -> {
                    UserClassSummaryDTO summary = classSummaries.get(user.getId());
                    return new UserListItemDTO(
                            user.getId(),
                            user.getUsername(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail(),
                            user.getPhone(),
                            user.getRoleFlags(),
                            user.getIsActive(),
                            user.getCreatedAt(),
                            summary == null ? null : summary.grade(),
                            summary == null ? null : summary.section(),
                            summary == null ? null : summary.className()
                    );
                })
                .toList();

        return new PaginatedResponseDTO<>(
                items,
                userPage.getNumber() + 1,
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages()
        );
    }

    public User createTeacher(User request) {
        User teacher = new User();
        teacher.setUsername(request.getUsername());
        teacher.setEmail(request.getEmail());
        teacher.setPasswordHash(request.getPasswordHash());
        copyOptionalProfileFields(request, teacher);
        teacher.setRoleFlags(User.ROLE_TEACHER);
        teacher.setTeacherSubjects(request.getTeacherSubjects());
        return createUser(teacher);
    }

    public User createStudent(User request) {
        User student = new User();
        student.setUsername(request.getUsername());
        student.setEmail(request.getEmail());
        student.setPasswordHash(request.getPasswordHash());
        copyOptionalProfileFields(request, student);
        student.setRoleFlags(User.ROLE_STUDENT);
        student.setTeacherSubjects(null);
        return createUser(student);
    }

    public User createParent(User request) {
        User parent = new User();
        parent.setUsername(request.getUsername());
        parent.setEmail(request.getEmail());
        parent.setPasswordHash(request.getPasswordHash());
        copyOptionalProfileFields(request, parent);
        parent.setRoleFlags(User.ROLE_PARENT);
        parent.setTeacherSubjects(null);
        return createUser(parent);
    }

    private void validateRole(Integer role) {
        if (role == null) {
            return;
        }
        if (role <= 0 || (role & (role - 1)) != 0 || (role & ALL_ROLE_FLAGS) == 0) {
            throw new IllegalArgumentException("Unsupported role filter");
        }
    }

    private void validateRoleFlags(Integer roleFlags) {
        if (roleFlags == null || roleFlags <= 0 || (roleFlags & ~ALL_ROLE_FLAGS) != 0) {
            throw new IllegalArgumentException("Unsupported role flags");
        }
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        String requestedSort = sortBy == null || sortBy.isBlank() ? "createdAt" : sortBy.trim();
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (requestedSort) {
            case "createdAt" -> Sort.by(direction, "createdAt").and(Sort.by(direction, "id"));
            case "name" -> Sort.by(direction, "lastName", "firstName", "id");
            case "username" -> Sort.by(direction, "username").and(Sort.by(direction, "id"));
            case "email" -> Sort.by(direction, "email").and(Sort.by(direction, "id"));
            case "roleFlags" -> Sort.by(direction, "roleFlags").and(Sort.by(direction, "id"));
            default -> throw new IllegalArgumentException("Unsupported sortBy value");
        };
    }

    private Specification<User> buildSpecification(UserListQueryDTO query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (query.getSearch() != null && !query.getSearch().isBlank()) {
                String term = "%" + query.getSearch().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), term)
                ));
            }

            if (query.getRole() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        criteriaBuilder.mod(root.get("roleFlags"), query.getRole() * 2),
                        query.getRole()
                ));
            }

            if (query.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), query.getStatus()));
            }

            if (query.getGrade() != null || (query.getSection() != null && !query.getSection().isBlank())) {
                Join<User, StudentEnrollment> enrollmentJoin = root.join("studentEnrollments", JoinType.INNER);
                Join<StudentEnrollment, Class> classJoin = enrollmentJoin.join("classEntity", JoinType.INNER);

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(enrollmentJoin.get("status"), StudentEnrollment.Status.ACTIVE),
                        criteriaBuilder.equal(enrollmentJoin.get("status"), StudentEnrollment.Status.active)
                ));

                if (query.getGrade() != null) {
                    predicates.add(criteriaBuilder.equal(classJoin.get("grade"), query.getGrade()));
                }

                if (query.getSection() != null && !query.getSection().isBlank()) {
                    predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.lower(classJoin.get("section")),
                            query.getSection().trim().toLowerCase(Locale.ROOT)
                    ));
                }
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Map<Long, UserClassSummaryDTO> loadClassSummaries(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, UserClassSummaryDTO> summaries = new LinkedHashMap<>();
        for (UserClassSummaryDTO summary : studentEnrollmentRepository.findActiveClassSummariesByStudentIds(userIds)) {
            summaries.putIfAbsent(summary.studentId(), summary);
        }
        return summaries;
    }
}
