CREATE TABLE homework_attachments (
    id BIGSERIAL PRIMARY KEY,
    homework_id BIGINT NOT NULL REFERENCES homework(id) ON UPDATE CASCADE ON DELETE CASCADE,
    original_filename VARCHAR(255) NOT NULL,
    stored_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_homework_attachments_homework_id
    ON homework_attachments(homework_id);
