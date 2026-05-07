CREATE TABLE university_service_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(80) UNIQUE NOT NULL,
    name VARCHAR(160) NOT NULL,
    default_office VARCHAR(120) NOT NULL,
    sla_days INTEGER NOT NULL DEFAULT 5 CHECK (sla_days > 0),
    requires_finance_clearance BOOLEAN NOT NULL DEFAULT FALSE,
    requires_attachment BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_university_service_types_active ON university_service_types(active);

INSERT INTO university_service_types (code, name, default_office, sla_days, requires_finance_clearance, requires_attachment)
VALUES
    ('TRANSCRIPT', 'Transcript request', 'Registrar', 5, TRUE, FALSE),
    ('ENROLLMENT_CERTIFICATE', 'Enrollment certificate', 'Registrar', 3, TRUE, FALSE),
    ('LEAVE_REQUEST', 'Leave request', 'Student affairs', 7, FALSE, TRUE),
    ('ADVISING_APPOINTMENT', 'Advising appointment', 'Student affairs', 2, FALSE, FALSE)
ON CONFLICT (code) DO NOTHING;
