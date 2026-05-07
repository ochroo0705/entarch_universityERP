ALTER TABLE parent_message_draft
    RENAME COLUMN subject TO current_subject;

ALTER TABLE parent_message_draft
    RENAME COLUMN message_body TO current_message_body;

ALTER TABLE parent_message_draft
    ADD COLUMN issue_type VARCHAR(50),
    ADD COLUMN teacher_note VARCHAR(1000),
    ADD COLUMN goal_label VARCHAR(100),
    ADD COLUMN generated_subject VARCHAR(255),
    ADD COLUMN generated_message_body TEXT,
    ADD COLUMN generation_provider VARCHAR(100),
    ADD COLUMN generation_model VARCHAR(100),
    ADD COLUMN provider_request_id VARCHAR(150),
    ADD COLUMN generation_prompt_version VARCHAR(100),
    ADD COLUMN generation_input_redacted_json JSONB,
    ADD COLUMN generation_output_redacted_json JSONB,
    ADD COLUMN generation_error_code VARCHAR(100),
    ADD COLUMN generation_error_message TEXT,
    ADD COLUMN generated_at TIMESTAMP,
    ADD COLUMN rejected_by_user_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    ADD COLUMN rejected_at TIMESTAMP,
    ADD COLUMN rejection_reason TEXT,
    ADD COLUMN last_edited_by_user_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL;

UPDATE parent_message_draft
SET draft_status = CASE
    WHEN draft_status = 'DRAFT' THEN 'READY_FOR_REVIEW'
    ELSE draft_status
END;

UPDATE parent_message_draft
SET issue_type = 'GENERAL_FOLLOW_UP'
WHERE issue_type IS NULL;

UPDATE parent_message_draft
SET generated_subject = current_subject,
    generated_message_body = current_message_body,
    generation_provider = COALESCE(generation_provider, 'LEGACY_PLACEHOLDER'),
    generation_model = COALESCE(generation_model, 'phase1-placeholder'),
    generation_prompt_version = COALESCE(generation_prompt_version, 'phase1'),
    generated_at = COALESCE(generated_at, created_at),
    last_edited_by_user_id = COALESCE(last_edited_by_user_id, created_by_user_id)
WHERE generated_subject IS NULL OR generated_message_body IS NULL;

ALTER TABLE parent_message_draft
    ALTER COLUMN issue_type SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_parent_message_draft_creator_created_at
    ON parent_message_draft(created_by_user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_parent_message_draft_provider_created_at
    ON parent_message_draft(generation_provider, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_parent_message_draft_approved_at
    ON parent_message_draft(approved_at DESC);

ALTER TABLE ai_audit_log
    ADD COLUMN provider_name VARCHAR(100),
    ADD COLUMN provider_model VARCHAR(100),
    ADD COLUMN correlation_id VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_ai_audit_log_event_status_created_at
    ON ai_audit_log(event_type, action_status, created_at DESC);
