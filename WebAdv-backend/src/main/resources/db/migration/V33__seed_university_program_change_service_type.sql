INSERT INTO university_service_types (code, name, default_office, sla_days, requires_finance_clearance, requires_attachment)
VALUES
    ('PROGRAM_CHANGE', 'Program change request', 'Registrar', 10, TRUE, TRUE),
    ('GRADUATION_CLEARANCE', 'Graduation clearance', 'Registrar', 10, TRUE, TRUE)
ON CONFLICT (code) DO NOTHING;
