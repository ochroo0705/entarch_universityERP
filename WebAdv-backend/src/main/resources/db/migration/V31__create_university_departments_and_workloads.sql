CREATE TABLE university_departments (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE university_faculty_workloads (
    id BIGSERIAL PRIMARY KEY,
    faculty_profile_id BIGINT NOT NULL REFERENCES university_faculty_profiles(id) ON DELETE CASCADE,
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    teaching_credits INT NOT NULL DEFAULT 0,
    advising_credits INT NOT NULL DEFAULT 0,
    research_credits INT NOT NULL DEFAULT 0,
    committee_credits INT NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_university_faculty_workloads_profile_term
    ON university_faculty_workloads (faculty_profile_id, academic_year, semester);

INSERT INTO university_departments (code, name)
VALUES
    ('CS', 'Computer Science'),
    ('BUS', 'Business Administration'),
    ('REG', 'Registrar'),
    ('FIN', 'Finance Office')
ON CONFLICT (code) DO NOTHING;
