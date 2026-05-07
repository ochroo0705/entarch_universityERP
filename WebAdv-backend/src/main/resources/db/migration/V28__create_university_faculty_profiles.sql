CREATE TABLE university_faculty_profiles (
    id BIGSERIAL PRIMARY KEY,
    faculty_user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    employee_number VARCHAR(60) UNIQUE,
    department VARCHAR(120) NOT NULL,
    academic_rank VARCHAR(120),
    employment_status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    hire_date DATE,
    office_location VARCHAR(120),
    workload_target_credits INTEGER NOT NULL DEFAULT 12,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_university_faculty_profiles_department
    ON university_faculty_profiles(department, employment_status);
