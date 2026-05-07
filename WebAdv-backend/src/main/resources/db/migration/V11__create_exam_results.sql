CREATE TABLE exam_results (
    id BIGSERIAL PRIMARY KEY,
    exam_schedule_id BIGINT NOT NULL REFERENCES exam_schedules(id) ON UPDATE CASCADE ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    score NUMERIC(7,2) NOT NULL,
    total_score NUMERIC(7,2) NOT NULL,
    percentage NUMERIC(5,2) NOT NULL,
    weighting NUMERIC(5,2),
    teacher_comment TEXT,
    remarks TEXT,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    recorded_by BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_exam_results_exam_student UNIQUE (exam_schedule_id, student_id),
    CONSTRAINT chk_exam_results_score_non_negative CHECK (score >= 0),
    CONSTRAINT chk_exam_results_total_positive CHECK (total_score > 0),
    CONSTRAINT chk_exam_results_percentage_range CHECK (percentage >= 0 AND percentage <= 100),
    CONSTRAINT chk_exam_results_weighting_range CHECK (weighting IS NULL OR (weighting >= 0 AND weighting <= 100))
);

CREATE INDEX idx_exam_results_exam_schedule_id ON exam_results (exam_schedule_id);
CREATE INDEX idx_exam_results_student_id ON exam_results (student_id);
CREATE INDEX idx_exam_results_published ON exam_results (published);
