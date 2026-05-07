CREATE TABLE university_course_prerequisites (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON UPDATE CASCADE ON DELETE CASCADE,
    prerequisite_subject_id BIGINT NOT NULL REFERENCES subjects(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_university_course_prerequisite UNIQUE (subject_id, prerequisite_subject_id),
    CONSTRAINT chk_university_course_prerequisite_not_self CHECK (subject_id <> prerequisite_subject_id)
);

CREATE TABLE university_academic_records (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    academic_year VARCHAR(20) NOT NULL,
    semester INTEGER NOT NULL,
    final_grade NUMERIC(5, 2),
    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('COMPLETED','FAILED')),
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_university_academic_record UNIQUE (student_id, subject_id)
);

CREATE INDEX idx_university_course_prerequisites_subject ON university_course_prerequisites(subject_id);
CREATE INDEX idx_university_academic_records_student ON university_academic_records(student_id);
