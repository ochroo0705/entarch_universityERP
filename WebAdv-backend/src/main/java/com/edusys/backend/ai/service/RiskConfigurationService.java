package com.edusys.backend.ai.service;

import com.edusys.backend.ai.dto.RiskConfigResponse;
import com.edusys.backend.ai.dto.RiskConfigUpdateRequest;
import com.edusys.backend.ai.model.RiskScoringConfig;
import com.edusys.backend.ai.repository.RiskScoringConfigRepository;
import com.edusys.backend.ai.validation.AiAccessService;
import com.edusys.backend.exception.ResourceNotFoundException;
import com.edusys.backend.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiskConfigurationService {

    private final RiskScoringConfigRepository riskScoringConfigRepository;
    private final AiAccessService aiAccessService;

    public RiskConfigurationService(RiskScoringConfigRepository riskScoringConfigRepository, AiAccessService aiAccessService) {
        this.riskScoringConfigRepository = riskScoringConfigRepository;
        this.aiAccessService = aiAccessService;
    }

    public RiskScoringConfig getActiveConfig() {
        return riskScoringConfigRepository.findFirstByIsActiveTrueOrderByUpdatedAtDesc()
                .orElseThrow(() -> new ResourceNotFoundException("Active risk scoring config not found"));
    }

    public RiskConfigResponse getActiveConfigResponse() {
        return toResponse(getActiveConfig());
    }

    @Transactional
    public RiskConfigResponse updateConfig(String configKey, RiskConfigUpdateRequest request) {
        User actor = aiAccessService.requireCurrentUser();
        if (!actor.isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins can update risk scoring config");
        }

        boolean activate = request.activate() == null || request.activate();
        if (activate) {
            riskScoringConfigRepository.findAll().stream()
                    .filter(config -> config.getConfigKey().equalsIgnoreCase(configKey))
                    .forEach(config -> config.setIsActive(false));
        }

        RiskScoringConfig config = riskScoringConfigRepository.findByConfigKeyAndConfigVersion(configKey, request.configVersion())
                .orElseGet(RiskScoringConfig::new);
        config.setConfigKey(configKey);
        config.setConfigVersion(request.configVersion());
        config.setAttendanceWeight(request.attendanceWeight());
        config.setLatenessWeight(request.latenessWeight());
        config.setHomeworkWeight(request.homeworkWeight());
        config.setGradeWeight(request.gradeWeight());
        config.setLowMaxScore(request.lowMaxScore());
        config.setMediumMaxScore(request.mediumMaxScore());
        config.setAttendanceWindowDays(request.attendanceWindowDays());
        config.setHomeworkWindowDays(request.homeworkWindowDays());
        config.setGradeWindowDays(request.gradeWindowDays());
        config.setIsActive(activate);
        config.setCreatedByUser(actor);
        return toResponse(riskScoringConfigRepository.save(config));
    }

    public RiskConfigResponse toResponse(RiskScoringConfig config) {
        return new RiskConfigResponse(
                config.getConfigKey(),
                config.getConfigVersion(),
                config.getAttendanceWeight(),
                config.getLatenessWeight(),
                config.getHomeworkWeight(),
                config.getGradeWeight(),
                config.getLowMaxScore(),
                config.getMediumMaxScore(),
                config.getAttendanceWindowDays(),
                config.getHomeworkWindowDays(),
                config.getGradeWindowDays(),
                config.getIsActive()
        );
    }
}
