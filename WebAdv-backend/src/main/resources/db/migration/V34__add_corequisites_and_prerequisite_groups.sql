ALTER TABLE university_course_prerequisites
    ADD COLUMN IF NOT EXISTS group_code VARCHAR(80);

CREATE TABLE university_course_corequisites (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    corequisite_subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_university_course_corequisites UNIQUE (subject_id, corequisite_subject_id),
    CONSTRAINT chk_university_course_corequisite_self CHECK (subject_id <> corequisite_subject_id)
);

CREATE INDEX idx_university_course_corequisites_subject
    ON university_course_corequisites (subject_id);
