package com.edusys.backend.ai.repository;

import com.edusys.backend.ai.model.AiAuditLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AiAuditLogRepository extends JpaRepository<AiAuditLog, Long>, JpaSpecificationExecutor<AiAuditLog> {

    @Override
    @EntityGraph(attributePaths = {"actorUser", "targetStudent", "targetParentUser"})
    Page<AiAuditLog> findAll(Specification<AiAuditLog> spec, Pageable pageable);
}
