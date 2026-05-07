ALTER TABLE university_integration_runs
    ADD COLUMN IF NOT EXISTS retry_count INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS error_message TEXT;

CREATE TABLE university_integration_connections (
    id BIGSERIAL PRIMARY KEY,
    integration_key VARCHAR(60) NOT NULL UNIQUE,
    display_name VARCHAR(160) NOT NULL,
    endpoint_url VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_status VARCHAR(40) NOT NULL DEFAULT 'READY',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO university_integration_connections (integration_key, display_name, endpoint_url, enabled, last_status)
VALUES
    ('lms', 'Learning Management System', 'mock://lms/roster', TRUE, 'READY'),
    ('bank', 'Bank payment gateway', 'mock://bank/payment-callback', TRUE, 'READY'),
    ('notification', 'Notification service', 'mock://notification/dispatch', TRUE, 'READY'),
    ('government', 'Government reporting', 'mock://government/statutory-report', TRUE, 'READY')
ON CONFLICT (integration_key) DO NOTHING;
