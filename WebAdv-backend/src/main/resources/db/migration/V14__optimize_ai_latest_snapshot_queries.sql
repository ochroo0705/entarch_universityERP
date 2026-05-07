CREATE INDEX IF NOT EXISTS idx_student_risk_snapshot_student_calculated_id
    ON student_risk_snapshot(student_id, calculated_at DESC, id DESC);
