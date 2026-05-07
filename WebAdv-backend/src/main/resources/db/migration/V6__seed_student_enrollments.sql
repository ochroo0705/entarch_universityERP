-- Deprecated migration.
-- Student enrollment demo data is seeded by DataLoader after users and classes exist.
-- Keeping this migration as a no-op avoids brittle foreign-key assumptions on serial IDs.

SELECT 1;
