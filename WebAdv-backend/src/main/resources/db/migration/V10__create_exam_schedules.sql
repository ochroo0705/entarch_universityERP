CREATE TABLE exam_schedules (
    id BIGSERIAL PRIMARY KEY,
    teaching_assignment_id BIGINT NOT NULL,
    exam_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room_number VARCHAR(30),
    title VARCHAR(255) NOT NULL,
    notes TEXT,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exam_schedules_teaching_assignment
        FOREIGN KEY (teaching_assignment_id) REFERENCES teaching_assignments(id)
);

CREATE INDEX idx_exam_schedules_exam_date ON exam_schedules (exam_date);
CREATE INDEX idx_exam_schedules_teaching_assignment_id ON exam_schedules (teaching_assignment_id);
CREATE INDEX idx_exam_schedules_published_active ON exam_schedules (published, is_active);
