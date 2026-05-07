ALTER TABLE student_risk_snapshot
    ADD COLUMN calculation_window_start TIMESTAMP,
    ADD COLUMN calculation_window_end TIMESTAMP,
    ADD COLUMN scoring_config_version VARCHAR(100),
    ADD COLUMN calculation_trigger VARCHAR(30),
    ADD COLUMN calculation_error TEXT,
    ADD COLUMN class_id BIGINT REFERENCES classes(id) ON UPDATE CASCADE ON DELETE SET NULL,
    ADD COLUMN grade_level INTEGER;

CREATE INDEX idx_student_risk_snapshot_calculated_at
    ON student_risk_snapshot(calculated_at DESC);
CREATE INDEX idx_student_risk_snapshot_risk_level_calculated_at
    ON student_risk_snapshot(risk_level, calculated_at DESC);
CREATE INDEX idx_student_risk_snapshot_class_calculated_at
    ON student_risk_snapshot(class_id, calculated_at DESC);
CREATE INDEX idx_student_risk_snapshot_grade_level_calculated_at
    ON student_risk_snapshot(grade_level, calculated_at DESC);

CREATE TABLE student_risk_indicator_snapshot (
    id BIGSERIAL PRIMARY KEY,
    risk_snapshot_id BIGINT NOT NULL REFERENCES student_risk_snapshot(id) ON UPDATE CASCADE ON DELETE CASCADE,
    indicator_code VARCHAR(30) NOT NULL,
    raw_value NUMERIC(8, 2),
    normalized_risk_value NUMERIC(8, 2) NOT NULL,
    weight NUMERIC(8, 4) NOT NULL,
    weighted_contribution NUMERIC(8, 2) NOT NULL,
    data_points_count INTEGER NOT NULL DEFAULT 0,
    is_missing_data BOOLEAN NOT NULL DEFAULT FALSE,
    details_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_student_risk_indicator_snapshot_snapshot
    ON student_risk_indicator_snapshot(risk_snapshot_id);
CREATE INDEX idx_student_risk_indicator_snapshot_code_snapshot
    ON student_risk_indicator_snapshot(indicator_code, risk_snapshot_id);

CREATE TABLE risk_scoring_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL,
    config_version VARCHAR(100) NOT NULL,
    attendance_weight NUMERIC(8, 4) NOT NULL,
    lateness_weight NUMERIC(8, 4) NOT NULL,
    homework_weight NUMERIC(8, 4) NOT NULL,
    grade_weight NUMERIC(8, 4) NOT NULL,
    low_max_score INTEGER NOT NULL,
    medium_max_score INTEGER NOT NULL,
    attendance_window_days INTEGER NOT NULL,
    homework_window_days INTEGER NOT NULL,
    grade_window_days INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_user_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_risk_scoring_config_key_version UNIQUE (config_key, config_version)
);

CREATE INDEX idx_risk_scoring_config_active
    ON risk_scoring_config(is_active);

INSERT INTO risk_scoring_config (
    config_key,
    config_version,
    attendance_weight,
    lateness_weight,
    homework_weight,
    grade_weight,
    low_max_score,
    medium_max_score,
    attendance_window_days,
    homework_window_days,
    grade_window_days,
    is_active
) VALUES (
    'DEFAULT',
    'rule-engine-v1',
    0.3500,
    0.1500,
    0.2500,
    0.2500,
    34,
    64,
    45,
    30,
    90,
    TRUE
);
