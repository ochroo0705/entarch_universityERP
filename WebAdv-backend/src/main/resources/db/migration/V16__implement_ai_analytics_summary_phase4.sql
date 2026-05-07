CREATE TABLE analytics_summary (
    id BIGSERIAL PRIMARY KEY,
    summary_type VARCHAR(50) NOT NULL,
    scope_type VARCHAR(50) NOT NULL,
    scope_key VARCHAR(100) NOT NULL,
    scope_label VARCHAR(255),
    requested_by_user_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    generated_for_role VARCHAR(30) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    comparison_period_start DATE,
    comparison_period_end DATE,
    input_fingerprint VARCHAR(128) NOT NULL,
    input_redacted_json JSONB NOT NULL,
    summary_json JSONB,
    headline VARCHAR(255),
    overall_summary_text TEXT,
    provider_name VARCHAR(100),
    provider_model VARCHAR(100),
    provider_request_id VARCHAR(150),
    prompt_version VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    generation_error_code VARCHAR(100),
    generation_error_message TEXT,
    is_placeholder BOOLEAN NOT NULL DEFAULT FALSE,
    generated_at TIMESTAMP,
    stale_after TIMESTAMP,
    last_viewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_analytics_summary_scope_generated_at
    ON analytics_summary(summary_type, scope_type, scope_key, period_start, period_end, generated_at DESC);
CREATE INDEX idx_analytics_summary_input_fingerprint
    ON analytics_summary(input_fingerprint);
CREATE INDEX idx_analytics_summary_status_stale_after
    ON analytics_summary(status, stale_after);
CREATE INDEX idx_analytics_summary_role_generated_at
    ON analytics_summary(generated_for_role, generated_at DESC);

CREATE TABLE analytics_summary_request (
    id BIGSERIAL PRIMARY KEY,
    analytics_summary_id BIGINT REFERENCES analytics_summary(id) ON UPDATE CASCADE ON DELETE SET NULL,
    summary_type VARCHAR(50) NOT NULL,
    scope_type VARCHAR(50) NOT NULL,
    scope_key VARCHAR(100) NOT NULL,
    request_mode VARCHAR(30) NOT NULL,
    requested_by_user_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    force_refresh BOOLEAN NOT NULL DEFAULT FALSE,
    request_status VARCHAR(30) NOT NULL,
    input_fingerprint VARCHAR(128),
    error_code VARCHAR(100),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_analytics_summary_request_scope_created_at
    ON analytics_summary_request(summary_type, scope_type, scope_key, created_at DESC);
CREATE INDEX idx_analytics_summary_request_status_created_at
    ON analytics_summary_request(request_status, created_at DESC);
CREATE INDEX idx_analytics_summary_request_requested_by
    ON analytics_summary_request(requested_by_user_id, created_at DESC);
