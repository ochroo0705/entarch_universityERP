CREATE TABLE university_faculty_leave_requests (
    id BIGSERIAL PRIMARY KEY,
    faculty_profile_id BIGINT NOT NULL REFERENCES university_faculty_profiles(id) ON DELETE CASCADE,
    leave_type VARCHAR(80) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'REQUESTED',
    reason TEXT,
    decision_notes TEXT,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_at TIMESTAMP,
    CONSTRAINT chk_university_faculty_leave_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_university_faculty_leave_profile
    ON university_faculty_leave_requests (faculty_profile_id);

CREATE INDEX idx_university_faculty_leave_status
    ON university_faculty_leave_requests (status);
