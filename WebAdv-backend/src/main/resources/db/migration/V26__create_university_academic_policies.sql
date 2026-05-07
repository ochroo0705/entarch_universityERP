CREATE TABLE university_academic_policies (
    id BIGSERIAL PRIMARY KEY,
    policy_name VARCHAR(120) NOT NULL,
    min_term_credits INTEGER NOT NULL DEFAULT 12,
    max_term_credits INTEGER NOT NULL DEFAULT 18,
    probation_max_term_credits INTEGER NOT NULL DEFAULT 12,
    min_average_grade_good_standing DECIMAL(5,2) NOT NULL DEFAULT 60.00,
    block_registration_when_probation BOOLEAN NOT NULL DEFAULT FALSE,
    allow_repeat_completed_courses BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_university_academic_policies_active ON university_academic_policies(active);

INSERT INTO university_academic_policies (
    policy_name,
    min_term_credits,
    max_term_credits,
    probation_max_term_credits,
    min_average_grade_good_standing,
    block_registration_when_probation,
    allow_repeat_completed_courses,
    active
) VALUES (
    'Default undergraduate selection policy',
    12,
    18,
    12,
    60.00,
    FALSE,
    FALSE,
    TRUE
);
