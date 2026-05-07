CREATE INDEX IF NOT EXISTS idx_teaching_assignments_is_active ON teaching_assignments (is_active);
CREATE INDEX IF NOT EXISTS idx_teaching_assignments_year_semester ON teaching_assignments (academic_year, semester);
CREATE INDEX IF NOT EXISTS idx_teaching_assignments_teacher_id ON teaching_assignments (teacher_id);
CREATE INDEX IF NOT EXISTS idx_teaching_assignments_subject_id ON teaching_assignments (subject_id);
CREATE INDEX IF NOT EXISTS idx_teaching_assignments_class_id ON teaching_assignments (class_id);

CREATE INDEX IF NOT EXISTS idx_student_enrollment_status ON student_enrollment (status);
CREATE INDEX IF NOT EXISTS idx_student_enrollment_class_id ON student_enrollment (class_id);

CREATE INDEX IF NOT EXISTS idx_subjects_grade_level ON subjects (grade_level);
CREATE INDEX IF NOT EXISTS idx_subjects_is_mandatory ON subjects (is_mandatory);
CREATE INDEX IF NOT EXISTS idx_subjects_subject_code ON subjects (subject_code);
CREATE INDEX IF NOT EXISTS idx_subjects_created_at ON subjects (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_schedules_day_period ON schedules (day_of_week, period_number);
CREATE INDEX IF NOT EXISTS idx_schedules_teaching_assignment_id ON schedules (teaching_assignment_id);
CREATE INDEX IF NOT EXISTS idx_schedules_room_number ON schedules (room_number);
