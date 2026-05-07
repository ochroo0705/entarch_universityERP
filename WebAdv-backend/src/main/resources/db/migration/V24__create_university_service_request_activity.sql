CREATE TABLE university_service_request_comments (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES university_service_requests(id) ON UPDATE CASCADE ON DELETE CASCADE,
    author_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    comment_text TEXT NOT NULL,
    internal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE university_service_request_history (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES university_service_requests(id) ON UPDATE CASCADE ON DELETE CASCADE,
    actor_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    event_type VARCHAR(80) NOT NULL,
    from_status VARCHAR(30),
    to_status VARCHAR(30),
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE university_service_request_attachments (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES university_service_requests(id) ON UPDATE CASCADE ON DELETE CASCADE,
    uploaded_by BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_university_service_request_comments_request ON university_service_request_comments(request_id);
CREATE INDEX idx_university_service_request_history_request ON university_service_request_history(request_id);
CREATE INDEX idx_university_service_request_attachments_request ON university_service_request_attachments(request_id);
