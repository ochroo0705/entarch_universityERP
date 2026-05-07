CREATE TABLE university_erp_event_logs (
    id BIGSERIAL PRIMARY KEY,
    module VARCHAR(80) NOT NULL,
    action VARCHAR(120) NOT NULL,
    entity_type VARCHAR(120),
    entity_id BIGINT,
    actor_user_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    student_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_university_erp_event_logs_created ON university_erp_event_logs(created_at DESC);
CREATE INDEX idx_university_erp_event_logs_module ON university_erp_event_logs(module);
CREATE INDEX idx_university_erp_event_logs_student ON university_erp_event_logs(student_id);
