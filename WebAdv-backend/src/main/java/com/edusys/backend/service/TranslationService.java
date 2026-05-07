package com.edusys.backend.service;

import com.edusys.backend.dto.TranslationDTO;
import com.edusys.backend.model.Translation;
import com.edusys.backend.repository.TranslationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TranslationService {

    private final TranslationRepository repository;

    public TranslationService(TranslationRepository repository) {
        this.repository = repository;
    }

    /**
     * Get all translations for an entity (all locales, all fields).
     */
    public List<TranslationDTO> getTranslations(String entityType, Long entityId) {
        return repository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Get translations for an entity filtered by locale.
     */
    public List<TranslationDTO> getTranslations(String entityType, Long entityId, String locale) {
        return repository.findByEntityTypeAndEntityIdAndLocale(entityType, entityId, locale)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Get translations for multiple entities at once (batch).
     * Returns a map of entityId -> list of translations.
     */
    public Map<Long, List<TranslationDTO>> getTranslationsBatch(String entityType, List<Long> entityIds) {
        if (entityIds == null || entityIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return repository.findByEntityTypeAndEntityIdIn(entityType, entityIds)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.groupingBy(TranslationDTO::entityId));
    }

    /**
     * Set (upsert) a single translation.
     */
    @Transactional
    public TranslationDTO setTranslation(TranslationDTO dto) {
        Translation entity = repository
                .findByEntityTypeAndEntityIdAndFieldNameAndLocale(
                        dto.entityType(), dto.entityId(), dto.fieldName(), dto.locale())
                .orElse(new Translation(dto.entityType(), dto.entityId(), dto.fieldName(), dto.locale(), dto.value()));

        entity.setValue(dto.value());
        entity.setUpdatedAt(LocalDateTime.now());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        return toDTO(repository.save(entity));
    }

    /**
     * Set multiple translations at once (bulk upsert).
     */
    @Transactional
    public List<TranslationDTO> setTranslationsBulk(List<TranslationDTO> dtos) {
        List<TranslationDTO> results = new ArrayList<>();
        for (TranslationDTO dto : dtos) {
            results.add(setTranslation(dto));
        }
        return results;
    }

    /**
     * Delete all translations for an entity.
     */
    @Transactional
    public void deleteTranslations(String entityType, Long entityId) {
        repository.deleteByEntityTypeAndEntityId(entityType, entityId);
    }

    /**
     * Get a single translated value, with fallback to default locale.
     */
    public Optional<String> getTranslatedValue(String entityType, Long entityId, String fieldName, String locale) {
        return repository
                .findByEntityTypeAndEntityIdAndFieldNameAndLocale(entityType, entityId, fieldName, locale)
                .map(Translation::getValue);
    }

    private TranslationDTO toDTO(Translation t) {
        return new TranslationDTO(t.getId(), t.getEntityType(), t.getEntityId(),
                t.getFieldName(), t.getLocale(), t.getValue());
    }
}
