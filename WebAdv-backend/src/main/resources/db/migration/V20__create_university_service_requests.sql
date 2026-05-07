CREATE TABLE university_service_requests (
    id BIGSERIAL PRIMARY KEY,
    request_number VARCHAR(40) UNIQUE NOT NULL,
    student_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    request_type VARCHAR(120) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'REQUESTED' CHECK (status IN ('REQUESTED','REVIEW','APPROVED','DELIVERED','ON_HOLD','REJECTED')),
    assigned_office VARCHAR(120),
    hold_reason TEXT,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_university_service_requests_student ON university_service_requests(student_id);
CREATE INDEX idx_university_service_requests_status ON university_service_requests(status);
