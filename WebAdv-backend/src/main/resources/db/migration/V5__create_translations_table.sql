-- Generic translations table for bilingual content
-- Primary language is Mongolian (stored in main entity columns)
-- English translations are stored in this table
CREATE TABLE translations (
    id          BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   BIGINT       NOT NULL,
    field_name  VARCHAR(50)  NOT NULL,
    locale      VARCHAR(10)  NOT NULL,
    value       TEXT         NOT NULL,
    created_at  TIMESTAMP    DEFAULT NOW(),
    updated_at  TIMESTAMP    DEFAULT NOW(),
    UNIQUE(entity_type, entity_id, field_name, locale)
);

CREATE INDEX idx_translations_entity ON translations(entity_type, entity_id);
CREATE INDEX idx_translations_locale ON translations(entity_type, entity_id, locale);

-- Migrate existing subject_name (English) into translations table as 'en' locale
INSERT INTO translations (entity_type, entity_id, field_name, locale, value)
SELECT 'subject', id, 'name', 'en', subject_name
FROM subjects
WHERE subject_name IS NOT NULL AND subject_name <> '';

-- If Mongolian names exist, update the primary subject_name to Mongolian
UPDATE subjects SET subject_name = subject_name_mn
WHERE subject_name_mn IS NOT NULL AND subject_name_mn <> '';
