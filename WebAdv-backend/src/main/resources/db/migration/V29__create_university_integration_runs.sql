CREATE TABLE university_integration_runs (
    id BIGSERIAL PRIMARY KEY,
    integration_key VARCHAR(60) NOT NULL,
    integration_name VARCHAR(160) NOT NULL,
    direction VARCHAR(160) NOT NULL,
    status VARCHAR(40) NOT NULL,
    payload TEXT,
    result_message TEXT,
    actor_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    exchanged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_university_integration_runs_key_time
    ON university_integration_runs (integration_key, exchanged_at DESC, id DESC);

CREATE INDEX idx_university_integration_runs_status
    ON university_integration_runs (status);
