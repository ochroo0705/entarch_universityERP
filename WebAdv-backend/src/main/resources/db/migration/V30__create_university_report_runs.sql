CREATE TABLE university_report_definitions (
    id BIGSERIAL PRIMARY KEY,
    report_key VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    category VARCHAR(80) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE university_report_runs (
    id BIGSERIAL PRIMARY KEY,
    report_definition_id BIGINT NOT NULL REFERENCES university_report_definitions(id) ON DELETE CASCADE,
    status VARCHAR(40) NOT NULL,
    filters TEXT,
    snapshot_payload TEXT,
    row_count BIGINT NOT NULL DEFAULT 0,
    actor_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_university_report_runs_definition_time
    ON university_report_runs (report_definition_id, generated_at DESC, id DESC);

CREATE INDEX idx_university_report_runs_status
    ON university_report_runs (status);

INSERT INTO university_report_definitions (report_key, name, category, description)
VALUES
    ('enrollment_funnel', 'Enrollment funnel', 'Admissions', 'Applicant, acceptance, and student conversion summary'),
    ('finance_balance', 'Finance balance', 'Finance', 'Billing, payment, and outstanding balance summary'),
    ('student_services_sla', 'Student services SLA', 'Student services', 'Service request status and queue health summary'),
    ('faculty_workload', 'Faculty workload', 'HR and faculty', 'Faculty profile and teaching workload summary'),
    ('integration_health', 'Integration health', 'Integration', 'Latest integration run status summary')
ON CONFLICT (report_key) DO NOTHING;
