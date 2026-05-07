CREATE INDEX IF NOT EXISTS idx_users_role_flags ON users (role_flags);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users (is_active);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_student_enrollment_student_status ON student_enrollment (student_id, status);
CREATE INDEX IF NOT EXISTS idx_classes_grade_section ON classes (grade, section);
