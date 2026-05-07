package com.edusys.backend.service;

import com.edusys.backend.dto.ChildDashboardDTO;
import com.edusys.backend.dto.ChildSummaryDTO;
import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.dto.ParentStudentLinkDTO;
import com.edusys.backend.dto.ParentStudentListItemDTO;
import com.edusys.backend.dto.ParentStudentListQueryDTO;
import com.edusys.backend.model.Attendance;
import com.edusys.backend.model.HomeworkSubmission;
import com.edusys.backend.model.ParentStudent;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.AttendanceRepository;
import com.edusys.backend.repository.GradeRepository;
import com.edusys.backend.repository.HomeworkRepository;
import com.edusys.backend.repository.HomeworkSubmissionRepository;
import com.edusys.backend.repository.ScheduleRepository;
import com.edusys.backend.repository.UserRepository;
import com.edusys.backend.repository.ParentStudentRepository;
import com.edusys.backend.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ParentStudentService {

    private final ParentStudentRepository repo;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final HomeworkRepository homeworkRepository;
    private final HomeworkSubmissionRepository homeworkSubmissionRepository;
    private final ScheduleRepository scheduleRepository;
    private final GradeRepository gradeRepository;

    public ParentStudentService(
            ParentStudentRepository repo,
            UserRepository userRepository,
            AttendanceRepository attendanceRepository,
            HomeworkRepository homeworkRepository,
            HomeworkSubmissionRepository homeworkSubmissionRepository,
            ScheduleRepository scheduleRepository,
            GradeRepository gradeRepository
    ) {
        this.repo = repo;
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.homeworkRepository = homeworkRepository;
        this.homeworkSubmissionRepository = homeworkSubmissionRepository;
        this.scheduleRepository = scheduleRepository;
        this.gradeRepository = gradeRepository;
    }

    public ParentStudent save(ParentStudent p) {
        return repo.save(p);
    }

    public ParentStudent linkParentToStudent(ParentStudentLinkDTO dto) {
        User parent = userRepository.findById(dto.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
        User student = userRepository.findById(dto.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // role flags: parent=4, student=1
        if (!parent.isParent()) {
            throw new IllegalArgumentException("User " + dto.parentId() + " is not a PARENT (roleFlags=" + parent.getRoleFlags() + ")");
        }
        if (!student.isStudent()) {
            throw new IllegalArgumentException("User " + dto.studentId() + " is not a STUDENT (roleFlags=" + student.getRoleFlags() + ")");
        }

        ParentStudent ps = new ParentStudent();
        ps.setParent(parent);
        ps.setStudent(student);
        ps.setRelationship(dto.relationship());
        if (dto.isPrimaryContact() != null) {
            ps.setIsPrimaryContact(dto.isPrimaryContact());
        }
        return repo.save(ps);
    }

    public Optional<ParentStudent> findById(Long id) {
        return repo.findById(id);
    }

    public PaginatedResponseDTO<ParentStudentListItemDTO> listLinks(ParentStudentListQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        Page<ParentStudent> linkPage = repo.findAll(
                buildListSpecification(query),
                PageRequest.of(page - 1, pageSize, buildListSort(query.getSortBy(), query.getSortOrder()))
        );

        List<ParentStudentListItemDTO> items = linkPage.getContent().stream()
                .map(this::toListItemDTO)
                .toList();

        return new PaginatedResponseDTO<>(
                items,
                linkPage.getNumber() + 1,
                linkPage.getSize(),
                linkPage.getTotalElements(),
                linkPage.getTotalPages()
        );
    }

    public List<ParentStudent> findAll() {
        return repo.findAll();
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public List<ChildSummaryDTO> getChildrenForParent(User parentUser) {
        if (parentUser == null || parentUser.getId() == null) {
            throw new IllegalArgumentException("Parent user is required");
        }
        if (!parentUser.isParent()) {
            throw new IllegalArgumentException("Current user is not a PARENT");
        }

        List<ParentStudent> links = repo.findByParent_Id(parentUser.getId());

        // Deduplicate by student id while preserving order
        Map<Long, ChildSummaryDTO> children = new LinkedHashMap<>();
        for (ParentStudent link : links) {
            if (link.getStudent() == null || link.getStudent().getId() == null) continue;
            User student = link.getStudent();
            children.putIfAbsent(student.getId(), new ChildSummaryDTO(
                    student.getId(),
                    student.getUsername(),
                    student.getFirstName(),
                    student.getLastName(),
                    student.getEmail()
            ));
        }

        return children.values().stream().toList();
    }

    public List<ChildDashboardDTO> getChildDashboardForParent(User parentUser) {
        if (parentUser == null || parentUser.getId() == null) {
            throw new IllegalArgumentException("Parent user is required");
        }
        if (!parentUser.isParent()) {
            throw new IllegalArgumentException("Current user is not a PARENT");
        }

        List<Long> rawStudentIds = repo.findStudentIdsByParentId(parentUser.getId());
        // Deduplicate while preserving order
        Set<Long> seen = new java.util.LinkedHashSet<>();
        List<Long> studentIds = rawStudentIds.stream().filter(seen::add).toList();

        // Batch-load all students at once instead of N individual queries
        Map<Long, User> studentMap = userRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        final int today = LocalDate.now().getDayOfWeek().getValue(); // 1-7
        final List<Attendance.Status> attendedStatuses = List.of(
            Attendance.Status.present,
            Attendance.Status.late,
            Attendance.Status.excused,
            Attendance.Status.sick
        );
        final List<HomeworkSubmission.Status> submittedStatuses = List.of(
                HomeworkSubmission.Status.submitted,
                HomeworkSubmission.Status.late,
                HomeworkSubmission.Status.graded
        );

        return studentIds.stream().map(studentId -> {
            User student = studentMap.get(studentId);
            if (student == null) {
                throw new ResourceNotFoundException("Student not found: " + studentId);
            }

            long attendanceTotal = attendanceRepository.countByStudent_Id(studentId);
            long attendanceAttended = attendanceTotal == 0
                    ? 0
                    : attendanceRepository.countByStudent_IdAndStatusIn(studentId, attendedStatuses);
            Double attendanceRatePercent = attendanceTotal == 0
                    ? null
                    : Math.round(((attendanceAttended * 100.0) / attendanceTotal) * 100.0) / 100.0;

            long homeworkTotal = homeworkRepository.countAllByStudentId(studentId);
            long homeworkSubmitted = homeworkSubmissionRepository.countByStudent_IdAndStatusIn(studentId, submittedStatuses);

            long classesToday = scheduleRepository.countByStudentIdAndDayOfWeek(studentId, today);

            Double avg = gradeRepository.findAverageGradeValueByStudentId(studentId);
            Double overallGpa = avg == null ? null : Math.round(avg * 100.0) / 100.0;

            return new ChildDashboardDTO(
                    student.getId(),
                    student.getUsername(),
                    student.getFirstName(),
                    student.getLastName(),
                    attendanceRatePercent,
                    homeworkSubmitted,
                    homeworkTotal,
                    classesToday,
                    overallGpa
            );
        }).toList();
    }

    private ParentStudentListItemDTO toListItemDTO(ParentStudent link) {
        User parent = link.getParent();
        User student = link.getStudent();
        return new ParentStudentListItemDTO(
                link.getId(),
                parent == null ? null : new ParentStudentListItemDTO.UserSummaryDTO(
                        parent.getId(),
                        parent.getUsername(),
                        parent.getFirstName(),
                        parent.getLastName()
                ),
                student == null ? null : new ParentStudentListItemDTO.UserSummaryDTO(
                        student.getId(),
                        student.getUsername(),
                        student.getFirstName(),
                        student.getLastName()
                ),
                link.getRelationship() == null ? null : link.getRelationship().name(),
                link.getIsPrimaryContact()
        );
    }

    private Sort buildListSort(String sortBy, String sortOrder) {
        String requestedSort = sortBy == null || sortBy.isBlank() ? "id" : sortBy.trim();
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (requestedSort) {
            case "id" -> Sort.by(direction, "id");
            case "relationship" -> Sort.by(direction, "relationship").and(Sort.by(direction, "id"));
            case "primaryContact" -> Sort.by(direction, "isPrimaryContact").and(Sort.by(direction, "id"));
            case "parentName" -> Sort.by(direction, "parent.lastName", "parent.firstName", "id");
            case "studentName" -> Sort.by(direction, "student.lastName", "student.firstName", "id");
            default -> throw new IllegalArgumentException("Unsupported sortBy value");
        };
    }

    private Specification<ParentStudent> buildListSpecification(ParentStudentListQueryDTO query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            Join<ParentStudent, User> parentJoin = root.join("parent", JoinType.LEFT);
            Join<ParentStudent, User> studentJoin = root.join("student", JoinType.LEFT);

            criteriaQuery.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (query.getSearch() != null && !query.getSearch().isBlank()) {
                String term = "%" + query.getSearch().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(parentJoin.get("firstName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(parentJoin.get("lastName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(parentJoin.get("username")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(studentJoin.get("firstName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(studentJoin.get("lastName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(studentJoin.get("username")), term)
                ));
            }

            if (query.getRelationship() != null && !query.getRelationship().isBlank()) {
                String normalized = query.getRelationship().trim().toUpperCase(Locale.ROOT);
                try {
                    ParentStudent.Relationship relationship = ParentStudent.Relationship.valueOf(normalized);
                    predicates.add(criteriaBuilder.equal(root.get("relationship"), relationship));
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Unsupported relationship filter");
                }
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
