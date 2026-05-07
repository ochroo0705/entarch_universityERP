package com.edusys.backend.service;

import com.edusys.backend.dto.PaginatedResponseDTO;
import com.edusys.backend.dto.SubjectListQueryDTO;
import com.edusys.backend.dto.TranslationDTO;
import com.edusys.backend.model.Subject;
import com.edusys.backend.repository.SubjectRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final TranslationService translationService;

    public SubjectService(SubjectRepository subjectRepository, TranslationService translationService) {
        this.subjectRepository = subjectRepository;
        this.translationService = translationService;
    }

    public Subject save(Subject subject) {
        Subject saved = subjectRepository.save(subject);
        syncTranslations(saved);
        return saved;
    }

    public Optional<Subject> findById(Long id) {
        return subjectRepository.findById(id);
    }

    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    public PaginatedResponseDTO<Subject> listSubjects(SubjectListQueryDTO query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        Page<Subject> subjectPage = subjectRepository.findAll(
                buildListSpecification(query),
                PageRequest.of(page - 1, pageSize, buildListSort(query.getSortBy(), query.getSortOrder()))
        );

        return new PaginatedResponseDTO<>(
                subjectPage.getContent(),
                subjectPage.getNumber() + 1,
                subjectPage.getSize(),
                subjectPage.getTotalElements(),
                subjectPage.getTotalPages()
        );
    }

    public void delete(Long id) {
        translationService.deleteTranslations("subject", id);
        subjectRepository.deleteById(id);
    }

    private void syncTranslations(Subject subject) {
        if (subject.getName() != null) {
            translationService.setTranslation(new TranslationDTO(
                    null, "subject", subject.getId(), "name", "en", subject.getName()));
        }
        if (subject.getSubjectNameMn() != null) {
            translationService.setTranslation(new TranslationDTO(
                    null, "subject", subject.getId(), "name", "mn", subject.getSubjectNameMn()));
        }
    }

    private Sort buildListSort(String sortBy, String sortOrder) {
        String requestedSort = sortBy == null || sortBy.isBlank() ? "createdAt" : sortBy.trim();
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (requestedSort) {
            case "id" -> Sort.by(direction, "id");
            case "subjectName" -> Sort.by(direction, "subjectName").and(Sort.by(direction, "id"));
            case "subjectCode" -> Sort.by(direction, "subjectCode").and(Sort.by(direction, "id"));
            case "gradeLevel" -> Sort.by(direction, "gradeLevel").and(Sort.by(direction, "id"));
            case "hoursPerWeek" -> Sort.by(direction, "hoursPerWeek").and(Sort.by(direction, "id"));
            case "isMandatory" -> Sort.by(direction, "isMandatory").and(Sort.by(direction, "id"));
            case "createdAt" -> Sort.by(direction, "createdAt").and(Sort.by(direction, "id"));
            default -> throw new IllegalArgumentException("Unsupported sortBy value");
        };
    }

    private Specification<Subject> buildListSpecification(SubjectListQueryDTO query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getSearch() != null && !query.getSearch().isBlank()) {
                String term = "%" + query.getSearch().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("subjectName")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("subjectNameMn")), term),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("subjectCode")), term)
                ));
            }

            if (query.getGradeLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("gradeLevel"), query.getGradeLevel()));
            }

            if (query.getIsMandatory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isMandatory"), query.getIsMandatory()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
