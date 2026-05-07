CREATE TABLE student_risk_snapshot (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    snapshot_status VARCHAR(20) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    risk_score INTEGER NOT NULL,
    attendance_rate NUMERIC(5,2),
    missing_homework_count INTEGER NOT NULL DEFAULT 0,
    grade_average NUMERIC(5,2),
    source_summary_json JSONB,
    reason_summary TEXT,
    recommended_action TEXT,
    calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by_user_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    model_version_label VARCHAR(100),
    is_placeholder BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_student_risk_snapshot_student_calculated_at
    ON student_risk_snapshot(student_id, calculated_at DESC);
CREATE INDEX idx_student_risk_snapshot_risk_level
    ON student_risk_snapshot(risk_level);
CREATE INDEX idx_student_risk_snapshot_snapshot_status
    ON student_risk_snapshot(snapshot_status);
CREATE INDEX idx_student_risk_snapshot_created_by
    ON student_risk_snapshot(created_by_user_id);

CREATE TABLE parent_message_draft (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    parent_user_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    risk_snapshot_id BIGINT REFERENCES student_risk_snapshot(id) ON UPDATE CASCADE ON DELETE SET NULL,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    draft_status VARCHAR(20) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    subject VARCHAR(255),
    message_body TEXT NOT NULL,
    tone_label VARCHAR(50),
    language_code VARCHAR(10),
    generation_source VARCHAR(50),
    is_placeholder BOOLEAN NOT NULL DEFAULT TRUE,
    approved_by_user_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    approved_at TIMESTAMP,
    sent_at TIMESTAMP,
    last_edited_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_parent_message_draft_student_created_at
    ON parent_message_draft(student_id, created_at DESC);
CREATE INDEX idx_parent_message_draft_parent_status
    ON parent_message_draft(parent_user_id, draft_status);
CREATE INDEX idx_parent_message_draft_snapshot
    ON parent_message_draft(risk_snapshot_id);
CREATE INDEX idx_parent_message_draft_status_created_at
    ON parent_message_draft(draft_status, created_at DESC);

CREATE TABLE ai_audit_log (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    actor_user_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    target_student_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    target_parent_user_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    request_id VARCHAR(100),
    action_status VARCHAR(20) NOT NULL,
    reason_code VARCHAR(100),
    details_json JSONB,
    old_value_json JSONB,
    new_value_json JSONB,
    ip_address VARCHAR(64),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_audit_log_entity
    ON ai_audit_log(entity_type, entity_id, created_at DESC);
CREATE INDEX idx_ai_audit_log_actor
    ON ai_audit_log(actor_user_id, created_at DESC);
CREATE INDEX idx_ai_audit_log_student
    ON ai_audit_log(target_student_id, created_at DESC);
CREATE INDEX idx_ai_audit_log_event
    ON ai_audit_log(event_type, created_at DESC);
CREATE INDEX idx_ai_audit_log_created_at
    ON ai_audit_log(created_at DESC);
