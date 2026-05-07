package com.edusys.backend.ai.model;

public enum DraftStatus {
    REQUESTED,
    GENERATING,
    READY_FOR_REVIEW,
    GENERATION_FAILED,
    APPROVED,
    REJECTED
}
