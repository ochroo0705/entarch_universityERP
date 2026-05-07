package com.edusys.backend.ai.repository;

import com.edusys.backend.ai.model.RiskScoringConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskScoringConfigRepository extends JpaRepository<RiskScoringConfig, Long> {
    Optional<RiskScoringConfig> findFirstByIsActiveTrueOrderByUpdatedAtDesc();

    Optional<RiskScoringConfig> findByConfigKeyAndConfigVersion(String configKey, String configVersion);
}
