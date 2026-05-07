package com.edusys.backend.service;

import com.edusys.backend.dto.ClassCreateDTO;
import com.edusys.backend.dto.ClassResponseDTO;
import com.edusys.backend.dto.TeacherSummaryDTO;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.Class;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.ClassRepository;
import com.edusys.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClassService {

    private final ClassRepository classRepository;
    private final UserRepository userRepository;

    public ClassService(ClassRepository classRepository, UserRepository userRepository) {
        this.classRepository = classRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private boolean isTeacher(User user) {
        return user.getRoleFlags() != null && (user.getRoleFlags() & 2) != 0;
    }

    private boolean isAdmin(User user) {
        return user.getRoleFlags() != null && (user.getRoleFlags() & 8) != 0;
    }

    private void assertHomeroomTeacher(User teacher, Class classEntity) {
        if (classEntity.getHomeroomTeacher() == null
                || classEntity.getHomeroomTeacher().getId() == null
                || !classEntity.getHomeroomTeacher().getId().equals(teacher.getId())) {
            throw new AccessDeniedException("Only the homeroom teacher can manage assistant teachers");
        }
    }

    private TeacherSummaryDTO toTeacherSummary(User u) {
        return new TeacherSummaryDTO(u.getId(), u.getUsername(), u.getFirstName(), u.getLastName(), u.getEmail());
    }

    public ClassResponseDTO toResponseDTO(Class c) {
        return new ClassResponseDTO(
                c.getId(),
                c.getClassName(),
                c.getGrade(),
                c.getSection(),
                c.getHomeroomTeacher() == null ? null : c.getHomeroomTeacher().getId(),
                c.getHomeroomTeacher() == null ? null : c.getHomeroomTeacher().getUsername(),
                c.getRoomNumber(),
                c.getAcademicYear(),
                c.getStudentCount(),
                c.getIsActive(),
                c.getCreatedAt()
        );
    }


    @Transactional
    public ClassResponseDTO createClassAsAdmin(ClassCreateDTO dto) {
        User admin = getCurrentUser();
        if (!isAdmin(admin)) {
            throw new AccessDeniedException("Only admins can create classes");
        }

        Class classEntity = new Class();
        classEntity.setClassName(dto.className());
        classEntity.setGrade(dto.grade());
        classEntity.setSection(dto.section());
        classEntity.setRoomNumber(dto.roomNumber());
        classEntity.setAcademicYear(dto.academicYear());

        if (dto.homeroomTeacherId() != null) {
            User teacher = userRepository.findById(dto.homeroomTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
            if (!isTeacher(teacher)) {
                throw new IllegalArgumentException(
                        "User " + dto.homeroomTeacherId() + " is not a TEACHER (roleFlags=" + teacher.getRoleFlags() + ")"
                );
            }
            classEntity.setHomeroomTeacher(teacher);
        } else {
            classEntity.setHomeroomTeacher(null);
        }

        classEntity.setStudentCount(0);
        classEntity.setIsActive(true);
        classEntity.setCreatedAt(LocalDateTime.now());

        return toResponseDTO(classRepository.save(classEntity));
    }


    @Transactional
    public void deactivateClassAsAdmin(Long classId) {
        User admin = getCurrentUser();
        if (!isAdmin(admin)) {
            throw new AccessDeniedException("Only admins can deactivate classes");
        }

        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        classEntity.setIsActive(false);
        classRepository.save(classEntity);
    }

    public Class save(Class c) {
        return classRepository.save(c);
    }

    public Optional<Class> findById(Long id) {
        return classRepository.findById(id);
    }

    public List<Class> findAll() {
        return classRepository.findAll();
    }

    public void delete(Long id) {
        classRepository.deleteById(id);
    }

    public List<ClassResponseDTO> getClassesTaughtByCurrentTeacher() {
        User teacher = getCurrentUser();
        if (!isTeacher(teacher)) {
            throw new AccessDeniedException("Only teachers can view taught classes");
        }

        return classRepository.findActiveClassesTaughtByTeacher(teacher.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public List<TeacherSummaryDTO> getAssistantTeachersForClass(Long classId) {
        User teacher = getCurrentUser();
        if (!isTeacher(teacher)) {
            throw new AccessDeniedException("Only teachers can view assistant teachers");
        }

        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        assertHomeroomTeacher(teacher, classEntity);

        return classEntity.getAssistantTeachers().stream()
                .map(this::toTeacherSummary)
                .toList();
    }

    @Transactional
    public List<TeacherSummaryDTO> addAssistantTeacher(Long classId, Long assistantTeacherId) {
        User teacher = getCurrentUser();
        if (!isTeacher(teacher)) {
            throw new AccessDeniedException("Only teachers can add assistant teachers");
        }

        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        assertHomeroomTeacher(teacher, classEntity);

        User assistant = userRepository.findById(assistantTeacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        if (!isTeacher(assistant)) {
            throw new IllegalArgumentException(
                    "User " + assistantTeacherId + " is not a TEACHER (roleFlags=" + assistant.getRoleFlags() + ")"
            );
        }
        if (assistant.getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("Homeroom teacher cannot be added as an assistant");
        }

        classEntity.getAssistantTeachers().add(assistant);
        classRepository.save(classEntity);

        return classEntity.getAssistantTeachers().stream()
                .map(this::toTeacherSummary)
                .toList();
    }

    @Transactional
    public List<TeacherSummaryDTO> removeAssistantTeacher(Long classId, Long assistantTeacherId) {
        User teacher = getCurrentUser();
        if (!isTeacher(teacher)) {
            throw new AccessDeniedException("Only teachers can remove assistant teachers");
        }

        Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        assertHomeroomTeacher(teacher, classEntity);

        classEntity.getAssistantTeachers().removeIf(u -> u.getId() != null && u.getId().equals(assistantTeacherId));
        classRepository.save(classEntity);

        return classEntity.getAssistantTeachers().stream()
                .map(this::toTeacherSummary)
                .toList();
    }
}
