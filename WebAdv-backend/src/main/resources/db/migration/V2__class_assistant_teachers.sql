-- Assistant teachers for a class (homeroom teacher can add/remove)
CREATE TABLE class_assistant_teachers (
    class_id BIGINT NOT NULL REFERENCES classes(id) ON UPDATE CASCADE ON DELETE CASCADE,
    teacher_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (class_id, teacher_id)
);

CREATE INDEX idx_class_assistants_teacher_id ON class_assistant_teachers(teacher_id);
