package com.edusys.backend.service;

import com.edusys.backend.dto.LoginRequestDTO;
import com.edusys.backend.dto.LoginResponseDTO;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByUsername(loginRequest.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new BadCredentialsException("User account is inactive");
        }

        if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        List<String> roles = extractRoles(user.getRoleFlags());

        // Get primary role (first role from the list)
        String primaryRole = roles.isEmpty() ? "ROLE_STUDENT" : roles.get(0);

        // JWT subject should be username so downstream authorization can resolve the user
        String token = jwtService.generateToken(user.getId(), user.getUsername(), roles);

        return new LoginResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                primaryRole,
                roles,
                token
        );
    }

    private List<String> extractRoles(Integer roleFlags) {
        List<String> roles = new ArrayList<>();
        int flags = roleFlags == null ? 0 : roleFlags;

        if ((flags & User.ROLE_ADMIN) != 0) roles.add("ROLE_ADMIN");
        if ((flags & User.ROLE_TEACHER) != 0) roles.add("ROLE_TEACHER");
        if ((flags & User.ROLE_STUDENT) != 0) roles.add("ROLE_STUDENT");
        if ((flags & User.ROLE_PARENT) != 0) roles.add("ROLE_PARENT");
        if ((flags & User.ROLE_COUNSELOR) != 0) roles.add("ROLE_COUNSELOR");
        if ((flags & User.ROLE_NURSE) != 0) roles.add("ROLE_NURSE");
        if ((flags & User.ROLE_FINANCE_STAFF) != 0) roles.add("ROLE_FINANCE_STAFF");
        if ((flags & User.ROLE_LIBRARIAN) != 0) roles.add("ROLE_LIBRARIAN");
        if ((flags & User.ROLE_TRANSPORT_COORDINATOR) != 0) roles.add("ROLE_TRANSPORT_COORDINATOR");
        if ((flags & User.ROLE_ADMISSIONS_STAFF) != 0) roles.add("ROLE_ADMISSIONS_STAFF");
        if ((flags & User.ROLE_CAFETERIA_STAFF) != 0) roles.add("ROLE_CAFETERIA_STAFF");

        // Optional fallback if roleFlags is empty
        if (roles.isEmpty()) roles.add("ROLE_STUDENT");

        return roles;
    }
}

