package com.edusys.backend.service;

import com.edusys.backend.model.User;
import com.edusys.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<String> roles = new ArrayList<>();
        int flags = user.getRoleFlags() == null ? 0 : user.getRoleFlags();

        if ((flags & User.ROLE_ADMIN) != 0) roles.add("ADMIN");
        if ((flags & User.ROLE_TEACHER) != 0) roles.add("TEACHER");
        if ((flags & User.ROLE_STUDENT) != 0) roles.add("STUDENT");
        if ((flags & User.ROLE_PARENT) != 0) roles.add("PARENT");
        if ((flags & User.ROLE_COUNSELOR) != 0) roles.add("COUNSELOR");
        if ((flags & User.ROLE_NURSE) != 0) roles.add("NURSE");
        if ((flags & User.ROLE_FINANCE_STAFF) != 0) roles.add("FINANCE_STAFF");
        if ((flags & User.ROLE_LIBRARIAN) != 0) roles.add("LIBRARIAN");
        if ((flags & User.ROLE_TRANSPORT_COORDINATOR) != 0) roles.add("TRANSPORT_COORDINATOR");
        if ((flags & User.ROLE_ADMISSIONS_STAFF) != 0) roles.add("ADMISSIONS_STAFF");
        if ((flags & User.ROLE_CAFETERIA_STAFF) != 0) roles.add("CAFETERIA_STAFF");

        // optional fallback if roleFlags is empty
        if (roles.isEmpty()) roles.add("STUDENT");

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .roles(roles.toArray(new String[0]))
                .build();
    }
}
