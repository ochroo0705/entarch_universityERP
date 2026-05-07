ALTER TABLE university_service_requests
    ADD COLUMN assigned_user_id BIGINT NULL,
    ADD COLUMN due_at TIMESTAMP NULL;

ALTER TABLE university_service_requests
    ADD CONSTRAINT fk_university_service_requests_assigned_user
    FOREIGN KEY (assigned_user_id) REFERENCES users(id);

CREATE INDEX idx_university_service_requests_office_status_due
    ON university_service_requests(assigned_office, status, due_at);

CREATE INDEX idx_university_service_requests_assigned_user
    ON university_service_requests(assigned_user_id);

UPDATE university_service_requests
SET due_at = COALESCE(requested_at, CURRENT_TIMESTAMP) + INTERVAL '5 day'
WHERE due_at IS NULL;
