package com.edusys.backend.service;

import com.edusys.backend.model.User;
import com.edusys.backend.repository.ParentStudentRepository;
import com.edusys.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("studentAccess")
public class StudentAccessService {

    private static final int ROLE_STUDENT = 1;
    private static final int ROLE_TEACHER = 2;
    private static final int ROLE_PARENT = 4;
    private static final int ROLE_ADMIN = 8;

    private final UserRepository userRepository;
    private final ParentStudentRepository parentStudentRepository;

    public StudentAccessService(UserRepository userRepository, ParentStudentRepository parentStudentRepository) {
        this.userRepository = userRepository;
        this.parentStudentRepository = parentStudentRepository;
    }

    public boolean canAccessStudent(Long studentId) {
        if (studentId == null) return false;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;

        int flags = user.getRoleFlags() == null ? 0 : user.getRoleFlags();

        // Keep existing behavior permissive for staff
        if ((flags & ROLE_ADMIN) != 0) return true;
        if ((flags & ROLE_TEACHER) != 0) return true;

        // Student can only access their own data
        if ((flags & ROLE_STUDENT) != 0) {
            return user.getId() != null && user.getId().equals(studentId);
        }

        // Parent can access linked student data
        if ((flags & ROLE_PARENT) != 0) {
            return user.getId() != null && parentStudentRepository.existsByParent_IdAndStudent_Id(user.getId(), studentId);
        }

        return false;
    }
}
