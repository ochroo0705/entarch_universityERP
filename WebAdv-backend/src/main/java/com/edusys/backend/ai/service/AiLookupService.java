package com.edusys.backend.ai.service;

import com.edusys.backend.ai.dto.AiStudentOptionResponse;
import com.edusys.backend.ai.validation.AiAccessService;
import com.edusys.backend.model.User;
import com.edusys.backend.repository.ParentStudentRepository;
import com.edusys.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiLookupService {

    private final AiAccessService aiAccessService;
    private final UserRepository userRepository;
    private final ParentStudentRepository parentStudentRepository;

    public AiLookupService(
            AiAccessService aiAccessService,
            UserRepository userRepository,
            ParentStudentRepository parentStudentRepository
    ) {
        this.aiAccessService = aiAccessService;
        this.userRepository = userRepository;
        this.parentStudentRepository = parentStudentRepository;
    }

    public List<AiStudentOptionResponse> getAccessibleStudents() {
        User actor = aiAccessService.requireCurrentUser();
        List<User> students = actor.isAdmin()
                ? userRepository.findAllStudentsOrderByName()
                : loadTeacherStudents(actor);

        if (students.isEmpty()) {
            return List.of();
        }

        List<Long> studentIds = students.stream().map(User::getId).toList();
        Map<Long, List<AiStudentOptionResponse.AiParentOptionResponse>> parentsByStudentId = new LinkedHashMap<>();

        parentStudentRepository.findByStudent_IdIn(studentIds).forEach(link -> {
            parentsByStudentId.computeIfAbsent(link.getStudent().getId(), ignored -> new ArrayList<>())
                    .add(new AiStudentOptionResponse.AiParentOptionResponse(
                            link.getParent().getId(),
                            link.getParent().getFullName().trim()
                    ));
        });

        parentsByStudentId.replaceAll((studentId, parents) -> parents.stream().distinct().toList());

        return students.stream()
                .map(student -> new AiStudentOptionResponse(
                        student.getId(),
                        student.getFullName().trim(),
                        parentsByStudentId.getOrDefault(student.getId(), List.of())
                ))
                .toList();
    }

    private List<User> loadTeacherStudents(User actor) {
        List<Long> studentIds = aiAccessService.getAccessibleStudentIds(actor);
        if (studentIds.isEmpty()) {
            return List.of();
        }

        return userRepository.findByIdInOrderByLastNameAscFirstNameAscIdAsc(studentIds).stream()
                .filter(User::isStudent)
                .sorted(Comparator.comparing(User::getFullName))
                .toList();
    }
}
