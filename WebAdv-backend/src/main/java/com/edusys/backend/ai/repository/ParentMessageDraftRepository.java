package com.edusys.backend.ai.repository;

import com.edusys.backend.ai.model.ParentMessageDraft;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface ParentMessageDraftRepository extends JpaRepository<ParentMessageDraft, Long>, JpaSpecificationExecutor<ParentMessageDraft> {

    @Override
    @EntityGraph(attributePaths = {
            "student",
            "parentUser",
            "createdByUser",
            "approvedByUser",
            "rejectedByUser",
            "lastEditedByUser",
            "riskSnapshot"
    })
    java.util.List<ParentMessageDraft> findAll();

    @EntityGraph(attributePaths = {
            "student",
            "parentUser",
            "createdByUser",
            "approvedByUser",
            "rejectedByUser",
            "lastEditedByUser",
            "riskSnapshot"
    })
    List<ParentMessageDraft> findAll(Specification<ParentMessageDraft> spec, Sort sort);

    @Override
    @EntityGraph(attributePaths = {
            "student",
            "parentUser",
            "createdByUser",
            "approvedByUser",
            "rejectedByUser",
            "lastEditedByUser",
            "riskSnapshot"
    })
    Optional<ParentMessageDraft> findById(Long id);
}
