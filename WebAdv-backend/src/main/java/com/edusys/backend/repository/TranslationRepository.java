package com.edusys.backend.repository;

import com.edusys.backend.model.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {

    List<Translation> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<Translation> findByEntityTypeAndEntityIdAndLocale(String entityType, Long entityId, String locale);

    List<Translation> findByEntityTypeAndEntityIdIn(String entityType, List<Long> entityIds);

    Optional<Translation> findByEntityTypeAndEntityIdAndFieldNameAndLocale(
            String entityType, Long entityId, String fieldName, String locale);

    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);
}
