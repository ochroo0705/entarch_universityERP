-- Allow JPA enum values (FATHER/MOTHER/GUARDIAN/OTHER) in addition to the original lowercase values
-- so ParentStudent.Relationship can be persisted without violating the CHECK constraint.

ALTER TABLE parent_students
    DROP CONSTRAINT IF EXISTS parent_students_relationship_check;

ALTER TABLE parent_students
    ADD CONSTRAINT parent_students_relationship_check
        CHECK (relationship IN (
            'father','mother','guardian','other',
            'FATHER','MOTHER','GUARDIAN','OTHER'
        ));
