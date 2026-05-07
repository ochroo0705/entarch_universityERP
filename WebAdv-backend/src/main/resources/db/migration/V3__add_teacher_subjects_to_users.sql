-- Stores teacher's subjects as static text (e.g. comma-separated or JSON string)
-- Null for non-teacher users.
ALTER TABLE users
    ADD COLUMN teacher_subjects TEXT;
