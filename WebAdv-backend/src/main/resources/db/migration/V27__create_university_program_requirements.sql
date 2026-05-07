CREATE TABLE university_program_requirements (
    id BIGSERIAL PRIMARY KEY,
    program_name VARCHAR(160) NOT NULL,
    requirement_name VARCHAR(160) NOT NULL,
    subject_id BIGINT REFERENCES subjects(id) ON UPDATE CASCADE ON DELETE SET NULL,
    required_credits INTEGER NOT NULL DEFAULT 3,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_university_program_requirements_program
    ON university_program_requirements(program_name, active);

CREATE INDEX idx_university_program_requirements_subject
    ON university_program_requirements(subject_id);
