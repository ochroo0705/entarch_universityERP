-- University ERP Mongolian demonstration seed data
-- Run on a development database after all Flyway migrations have completed.
-- The script is idempotent for the named demo records and can be rerun safely.

BEGIN;

-- Demo users: role_flags = 1 student, 2 teacher/faculty, 8 admin.
INSERT INTO users (
    username, email, password_hash, first_name, last_name, role_flags,
    phone, address, date_of_birth, gender, is_active, teacher_subjects
) VALUES
    ('erp_admin_mn', 'erp.admin@demo.edu.mn', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Номин', 'Адъяа', 8, '99110011', 'Улаанбаатар, Сүхбаатар дүүрэг', '1988-03-14', 'F', true, null),
    ('erp_student_bat', 'bat.ochir@demo.edu.mn', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Бат-Очир', 'Ганболд', 1, '99112233', 'Улаанбаатар, Баянзүрх дүүрэг', '2005-10-02', 'M', true, null),
    ('erp_student_saruul', 'saruul.enkh@demo.edu.mn', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Саруул', 'Энхжаргал', 1, '99114455', 'Дархан-Уул аймаг', '2004-06-18', 'F', true, null),
    ('erp_student_temuulen', 'temuulen.bold@demo.edu.mn', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Тэмүүлэн', 'Болдбаатар', 1, '99116677', 'Орхон аймаг', '2005-01-27', 'M', true, null),
    ('erp_faculty_ariunaa', 'ariunaa.cs@demo.edu.mn', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ариунаа', 'Цэдэн', 2, '99001122', 'Улаанбаатар, Хан-Уул дүүрэг', '1982-09-09', 'F', true, 'Програмчлалын үндэс, Өгөгдлийн сан'),
    ('erp_faculty_munkhbayar', 'munkhbayar.bus@demo.edu.mn', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Мөнхбаяр', 'Дорж', 2, '99003344', 'Улаанбаатар, Чингэлтэй дүүрэг', '1979-12-05', 'M', true, 'Нягтлан бодох бүртгэл, Санхүүгийн удирдлага')
ON CONFLICT (username) DO UPDATE SET
    email = EXCLUDED.email,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    role_flags = EXCLUDED.role_flags,
    phone = EXCLUDED.phone,
    address = EXCLUDED.address,
    date_of_birth = EXCLUDED.date_of_birth,
    gender = EXCLUDED.gender,
    is_active = EXCLUDED.is_active,
    teacher_subjects = EXCLUDED.teacher_subjects,
    updated_at = CURRENT_TIMESTAMP;

-- University subjects/courses.
INSERT INTO subjects (subject_name, subject_name_mn, subject_code, grade_level, hours_per_week, is_mandatory)
VALUES
    ('Introduction to Programming', 'Програмчлалын үндэс', 'CS101', 1, 4, true),
    ('Database Systems', 'Өгөгдлийн сангийн систем', 'CS201', 2, 4, true),
    ('Academic Writing', 'Академик бичвэр', 'GEN101', 1, 3, true),
    ('Financial Accounting', 'Санхүүгийн нягтлан бодох бүртгэл', 'ACC101', 1, 3, true),
    ('Business Information Systems', 'Бизнесийн мэдээллийн систем', 'MIS201', 2, 3, false)
ON CONFLICT (subject_code) DO UPDATE SET
    subject_name = EXCLUDED.subject_name,
    subject_name_mn = EXCLUDED.subject_name_mn,
    grade_level = EXCLUDED.grade_level,
    hours_per_week = EXCLUDED.hours_per_week,
    is_mandatory = EXCLUDED.is_mandatory;

-- Admissions.
INSERT INTO university_applicants (
    application_number, first_name, last_name, email, phone, program, status,
    decision_notes, converted_student_id, submitted_at
) VALUES
    ('APP-MN-2026-001', 'Бат-Очир', 'Ганболд', 'bat.ochir.applicant@demo.edu.mn', '99112233', 'Мэдээллийн систем', 'CONVERTED', 'Бичиг баримт бүрэн, элсэлт баталгаажсан.', (SELECT id FROM users WHERE username = 'erp_student_bat'), '2026-04-15 10:20:00'),
    ('APP-MN-2026-002', 'Саруул', 'Энхжаргал', 'saruul.enkh.applicant@demo.edu.mn', '99114455', 'Бизнесийн удирдлага', 'ACCEPTED', 'Шалгалтын оноо шаардлага хангасан.', null, '2026-04-16 11:35:00'),
    ('APP-MN-2026-003', 'Анужин', 'Нямдорж', 'anujin.nyam@demo.edu.mn', '99118899', 'Компьютерын ухаан', 'SCREENING', 'Дүнгийн хуулбар шалгаж байна.', null, '2026-04-18 09:10:00')
ON CONFLICT (application_number) DO UPDATE SET
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    email = EXCLUDED.email,
    phone = EXCLUDED.phone,
    program = EXCLUDED.program,
    status = EXCLUDED.status,
    decision_notes = EXCLUDED.decision_notes,
    converted_student_id = EXCLUDED.converted_student_id,
    updated_at = CURRENT_TIMESTAMP;

-- Academic policy, prerequisites, co-requisites, and program requirements.
INSERT INTO university_academic_policies (
    policy_name, min_term_credits, max_term_credits, probation_max_term_credits,
    min_average_grade_good_standing, block_registration_when_probation,
    allow_repeat_completed_courses, active
)
SELECT 'Бакалаврын ерөнхий академик журам', 12, 18, 9, 60.00, true, false, true
WHERE NOT EXISTS (SELECT 1 FROM university_academic_policies WHERE policy_name = 'Бакалаврын ерөнхий академик журам');

INSERT INTO university_course_prerequisites (subject_id, prerequisite_subject_id, group_code)
SELECT s.id, p.id, 'CS-CORE-1'
FROM subjects s, subjects p
WHERE s.subject_code = 'CS201' AND p.subject_code = 'CS101'
ON CONFLICT (subject_id, prerequisite_subject_id) DO UPDATE SET group_code = EXCLUDED.group_code;

INSERT INTO university_course_corequisites (subject_id, corequisite_subject_id)
SELECT s.id, c.id
FROM subjects s, subjects c
WHERE s.subject_code = 'MIS201' AND c.subject_code = 'ACC101'
ON CONFLICT (subject_id, corequisite_subject_id) DO NOTHING;

INSERT INTO university_program_requirements (program_name, requirement_name, subject_id, required_credits, active)
SELECT 'Мэдээллийн систем', 'Мэргэжлийн суурь хичээл', s.id, 3, true
FROM subjects s
WHERE s.subject_code = 'CS101'
  AND NOT EXISTS (
      SELECT 1 FROM university_program_requirements
      WHERE program_name = 'Мэдээллийн систем' AND requirement_name = 'Мэргэжлийн суурь хичээл' AND subject_id = s.id
  );

INSERT INTO university_program_requirements (program_name, requirement_name, subject_id, required_credits, active)
SELECT 'Бизнесийн удирдлага', 'Санхүүгийн суурь хичээл', s.id, 3, true
FROM subjects s
WHERE s.subject_code = 'ACC101'
  AND NOT EXISTS (
      SELECT 1 FROM university_program_requirements
      WHERE program_name = 'Бизнесийн удирдлага' AND requirement_name = 'Санхүүгийн суурь хичээл' AND subject_id = s.id
  );

-- Barebones finance module.
INSERT INTO fee_items (name, description, category, amount, is_active)
SELECT '2026 хаврын сургалтын төлбөр', 'Мэдээллийн систем хөтөлбөрийн нэг улирлын сургалтын төлбөр', 'TUITION', 1800000.00, true
WHERE NOT EXISTS (SELECT 1 FROM fee_items WHERE name = '2026 хаврын сургалтын төлбөр');

INSERT INTO fee_items (name, description, category, amount, is_active)
SELECT 'Оюутны үйлчилгээний хураамж', 'Номын сан, тодорхойлолт, системийн үйлчилгээний багц хураамж', 'OTHER', 85000.00, true
WHERE NOT EXISTS (SELECT 1 FROM fee_items WHERE name = 'Оюутны үйлчилгээний хураамж');

INSERT INTO fee_invoices (student_id, invoice_number, due_date, status, notes)
VALUES
    ((SELECT id FROM users WHERE username = 'erp_student_bat'), 'INV-MN-2026-001', '2026-05-20', 'ISSUED', 'Хаврын улирлын сургалтын төлбөр'),
    ((SELECT id FROM users WHERE username = 'erp_student_saruul'), 'INV-MN-2026-002', '2026-05-20', 'PARTIALLY_PAID', 'Сургалтын төлбөрийн урьдчилгаа төлөгдсөн')
ON CONFLICT (invoice_number) DO UPDATE SET
    student_id = EXCLUDED.student_id,
    due_date = EXCLUDED.due_date,
    status = EXCLUDED.status,
    notes = EXCLUDED.notes,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO fee_invoice_lines (invoice_id, fee_item_id, description, amount)
SELECT i.id, f.id, 'Сургалтын төлбөр', 1800000.00
FROM fee_invoices i, fee_items f
WHERE i.invoice_number = 'INV-MN-2026-001'
  AND f.name = '2026 хаврын сургалтын төлбөр'
  AND NOT EXISTS (SELECT 1 FROM fee_invoice_lines WHERE invoice_id = i.id AND description = 'Сургалтын төлбөр');

INSERT INTO fee_invoice_lines (invoice_id, fee_item_id, description, amount)
SELECT i.id, f.id, 'Оюутны үйлчилгээний хураамж', 85000.00
FROM fee_invoices i, fee_items f
WHERE i.invoice_number = 'INV-MN-2026-001'
  AND f.name = 'Оюутны үйлчилгээний хураамж'
  AND NOT EXISTS (SELECT 1 FROM fee_invoice_lines WHERE invoice_id = i.id AND description = 'Оюутны үйлчилгээний хураамж');

INSERT INTO fee_invoice_lines (invoice_id, fee_item_id, description, amount)
SELECT i.id, f.id, 'Сургалтын төлбөрийн урьдчилгаа', 900000.00
FROM fee_invoices i, fee_items f
WHERE i.invoice_number = 'INV-MN-2026-002'
  AND f.name = '2026 хаврын сургалтын төлбөр'
  AND NOT EXISTS (SELECT 1 FROM fee_invoice_lines WHERE invoice_id = i.id AND description = 'Сургалтын төлбөрийн урьдчилгаа');

INSERT INTO fee_payments (invoice_id, student_id, amount, payment_date, method, status, reference_number, notes, recorded_by)
SELECT i.id, i.student_id, 900000.00, '2026-05-01 14:20:00', 'BANK_TRANSFER', 'COMPLETED', 'KHAN-DEMO-0001', 'Хаан банкны демо шилжүүлэг', (SELECT id FROM users WHERE username = 'erp_admin_mn')
FROM fee_invoices i
WHERE i.invoice_number = 'INV-MN-2026-002'
  AND NOT EXISTS (SELECT 1 FROM fee_payments WHERE reference_number = 'KHAN-DEMO-0001');

-- Course selection and academic records.
INSERT INTO university_course_selections (
    student_id, subject_id, academic_year, semester, credits, status, invoice_id
)
SELECT u.id, s.id, '2025-2026', 2, 3, 'BILLED', (SELECT id FROM fee_invoices WHERE invoice_number = 'INV-MN-2026-001')
FROM users u, subjects s
WHERE u.username = 'erp_student_bat' AND s.subject_code = 'CS101'
ON CONFLICT (student_id, subject_id, academic_year, semester) DO UPDATE SET
    credits = EXCLUDED.credits,
    status = EXCLUDED.status,
    invoice_id = EXCLUDED.invoice_id,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO university_course_selections (student_id, subject_id, academic_year, semester, credits, status, invoice_id)
SELECT u.id, s.id, '2025-2026', 2, 3, 'SELECTED', null
FROM users u, subjects s
WHERE u.username = 'erp_student_saruul' AND s.subject_code = 'ACC101'
ON CONFLICT (student_id, subject_id, academic_year, semester) DO UPDATE SET
    credits = EXCLUDED.credits,
    status = EXCLUDED.status,
    invoice_id = EXCLUDED.invoice_id,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO university_academic_records (student_id, subject_id, academic_year, semester, final_grade, status)
SELECT u.id, s.id, '2025-2026', 1, 86.50, 'COMPLETED'
FROM users u, subjects s
WHERE u.username = 'erp_student_bat' AND s.subject_code = 'CS101'
ON CONFLICT (student_id, subject_id) DO UPDATE SET
    academic_year = EXCLUDED.academic_year,
    semester = EXCLUDED.semester,
    final_grade = EXCLUDED.final_grade,
    status = EXCLUDED.status;

-- Student services.
INSERT INTO university_service_types (
    code, name, default_office, sla_days, requires_finance_clearance, requires_attachment, active
) VALUES
    ('TRANSCRIPT_MN', 'Дүнгийн хуулга авах', 'Сургалтын алба', 3, true, false, true),
    ('STUDENT_CERT_MN', 'Оюутны тодорхойлолт авах', 'Сургалтын алба', 2, false, false, true),
    ('PROGRAM_CHANGE_MN', 'Хөтөлбөр солих хүсэлт', 'Сургалтын бодлогын алба', 7, true, true, true)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    default_office = EXCLUDED.default_office,
    sla_days = EXCLUDED.sla_days,
    requires_finance_clearance = EXCLUDED.requires_finance_clearance,
    requires_attachment = EXCLUDED.requires_attachment,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO university_service_requests (
    request_number, student_id, request_type, description, status, assigned_office,
    hold_reason, requested_at, completed_at, assigned_user_id, due_at
) VALUES
    ('SRV-MN-2026-001', (SELECT id FROM users WHERE username = 'erp_student_bat'), 'Дүнгийн хуулга авах', 'Гадаад тэтгэлэгт бүртгүүлэхэд дүнгийн хуулга хэрэгтэй.', 'REVIEW', 'Сургалтын алба', null, '2026-05-02 09:40:00', null, (SELECT id FROM users WHERE username = 'erp_admin_mn'), '2026-05-05 18:00:00'),
    ('SRV-MN-2026-002', (SELECT id FROM users WHERE username = 'erp_student_saruul'), 'Оюутны тодорхойлолт авах', 'Дадлагын байгууллагад өгөх тодорхойлолт.', 'DELIVERED', 'Сургалтын алба', null, '2026-05-01 13:10:00', '2026-05-02 16:30:00', (SELECT id FROM users WHERE username = 'erp_admin_mn'), '2026-05-03 18:00:00'),
    ('SRV-MN-2026-003', (SELECT id FROM users WHERE username = 'erp_student_temuulen'), 'Хөтөлбөр солих хүсэлт', 'Компьютерын ухаанаас Мэдээллийн систем рүү шилжих хүсэлт.', 'ON_HOLD', 'Сургалтын бодлогын алба', 'Санхүүгийн үлдэгдэл шалгах шаардлагатай.', '2026-05-03 10:15:00', null, (SELECT id FROM users WHERE username = 'erp_admin_mn'), '2026-05-10 18:00:00')
ON CONFLICT (request_number) DO UPDATE SET
    student_id = EXCLUDED.student_id,
    request_type = EXCLUDED.request_type,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    assigned_office = EXCLUDED.assigned_office,
    hold_reason = EXCLUDED.hold_reason,
    completed_at = EXCLUDED.completed_at,
    assigned_user_id = EXCLUDED.assigned_user_id,
    due_at = EXCLUDED.due_at,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO university_service_request_comments (request_id, author_id, comment_text, internal)
SELECT r.id, (SELECT id FROM users WHERE username = 'erp_admin_mn'), 'Санхүүгийн төлөв шалгаад дүнгийн хуулга хэвлэх боломжтой.', true
FROM university_service_requests r
WHERE r.request_number = 'SRV-MN-2026-001'
  AND NOT EXISTS (SELECT 1 FROM university_service_request_comments WHERE request_id = r.id AND comment_text = 'Санхүүгийн төлөв шалгаад дүнгийн хуулга хэвлэх боломжтой.');

INSERT INTO university_service_request_history (request_id, actor_id, event_type, from_status, to_status, details)
SELECT r.id, (SELECT id FROM users WHERE username = 'erp_admin_mn'), 'STATUS_CHANGED', 'REQUESTED', r.status, 'Демо хүсэлтийн төлөв шинэчлэв.'
FROM university_service_requests r
WHERE r.request_number IN ('SRV-MN-2026-001', 'SRV-MN-2026-002', 'SRV-MN-2026-003')
  AND NOT EXISTS (SELECT 1 FROM university_service_request_history WHERE request_id = r.id AND event_type = 'STATUS_CHANGED' AND details = 'Демо хүсэлтийн төлөв шинэчлэв.');

-- HR/faculty module.
INSERT INTO university_departments (code, name, active)
VALUES
    ('CS', 'Компьютерын ухааны тэнхим', true),
    ('BUS', 'Бизнесийн удирдлагын тэнхим', true)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO university_faculty_profiles (
    faculty_user_id, employee_number, department, academic_rank, employment_status,
    hire_date, office_location, workload_target_credits
) VALUES
    ((SELECT id FROM users WHERE username = 'erp_faculty_ariunaa'), 'EMP-MN-1001', 'Компьютерын ухааны тэнхим', 'Дэд профессор', 'ACTIVE', '2016-09-01', 'A байр 304', 12),
    ((SELECT id FROM users WHERE username = 'erp_faculty_munkhbayar'), 'EMP-MN-1002', 'Бизнесийн удирдлагын тэнхим', 'Ахлах багш', 'ACTIVE', '2014-02-01', 'B байр 212', 12)
ON CONFLICT (faculty_user_id) DO UPDATE SET
    employee_number = EXCLUDED.employee_number,
    department = EXCLUDED.department,
    academic_rank = EXCLUDED.academic_rank,
    employment_status = EXCLUDED.employment_status,
    hire_date = EXCLUDED.hire_date,
    office_location = EXCLUDED.office_location,
    workload_target_credits = EXCLUDED.workload_target_credits,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO university_faculty_workloads (
    faculty_profile_id, academic_year, semester, teaching_credits, advising_credits,
    research_credits, committee_credits, notes
)
SELECT fp.id, '2025-2026', 2, 9, 2, 2, 1, 'CS101 болон CS201 хичээл, 18 оюутны зөвлөх үйлчилгээ.'
FROM university_faculty_profiles fp
JOIN users u ON u.id = fp.faculty_user_id
WHERE u.username = 'erp_faculty_ariunaa'
  AND NOT EXISTS (SELECT 1 FROM university_faculty_workloads WHERE faculty_profile_id = fp.id AND academic_year = '2025-2026' AND semester = 2);

INSERT INTO university_faculty_leave_requests (
    faculty_profile_id, leave_type, start_date, end_date, status, reason, decision_notes, decided_at
)
SELECT fp.id, 'Эрдэм шинжилгээний томилолт', '2026-06-10', '2026-06-14', 'APPROVED', 'Олон улсын их сургуулийн ERP семинарт илтгэл тавих.', 'Тэнхимийн эрхлэгч баталсан.', '2026-05-04 15:00:00'
FROM university_faculty_profiles fp
JOIN users u ON u.id = fp.faculty_user_id
WHERE u.username = 'erp_faculty_ariunaa'
  AND NOT EXISTS (SELECT 1 FROM university_faculty_leave_requests WHERE faculty_profile_id = fp.id AND start_date = '2026-06-10' AND end_date = '2026-06-14');

-- Integration configuration and exchange logs.
INSERT INTO university_integration_connections (
    integration_key, display_name, endpoint_url, enabled, last_status, adapter_mode, auth_type, secret_ref
) VALUES
    ('LMS_ROSTER_EXPORT', 'LMS рүү хичээл сонголтын жагсаалт экспортлох', 'https://lms.demo.edu.mn/api/rosters', true, 'READY', 'MOCK', 'API_KEY', 'vault/demo/lms-api-key'),
    ('GOV_STUDENT_REPORT', 'Боловсролын байгууллагын оюутны тайлан', 'https://gov.demo.edu.mn/api/university-students', true, 'READY', 'MOCK', 'API_KEY', 'vault/demo/gov-report-key'),
    ('BANK_PAYMENT_SYNC', 'Банкны төлбөрийн гүйлгээ татах', 'https://bank.demo.mn/api/payments', true, 'READY', 'MOCK', 'OAUTH2', 'vault/demo/bank-client')
ON CONFLICT (integration_key) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    endpoint_url = EXCLUDED.endpoint_url,
    enabled = EXCLUDED.enabled,
    last_status = EXCLUDED.last_status,
    adapter_mode = EXCLUDED.adapter_mode,
    auth_type = EXCLUDED.auth_type,
    secret_ref = EXCLUDED.secret_ref,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO university_integration_runs (
    integration_key, integration_name, direction, status, payload, result_message,
    actor_user_id, exchanged_at, retry_count, error_message
) SELECT 'LMS_ROSTER_EXPORT', 'LMS рүү хичээл сонголтын жагсаалт экспортлох', 'OUTBOUND', 'SUCCESS',
    '{"academicYear":"2025-2026","semester":2,"course":"CS101","students":1}',
    '1 оюутны сонголтыг LMS жагсаалтад илгээсэн.',
    (SELECT id FROM users WHERE username = 'erp_admin_mn'), '2026-05-03 12:00:00', 0, null
WHERE NOT EXISTS (SELECT 1 FROM university_integration_runs WHERE integration_key = 'LMS_ROSTER_EXPORT' AND exchanged_at = '2026-05-03 12:00:00');

INSERT INTO university_integration_runs (
    integration_key, integration_name, direction, status, payload, result_message,
    actor_user_id, exchanged_at, retry_count, error_message
) SELECT 'BANK_PAYMENT_SYNC', 'Банкны төлбөрийн гүйлгээ татах', 'INBOUND', 'SUCCESS',
    '{"referenceNumber":"KHAN-DEMO-0001","amount":900000}',
    'Банкны төлбөрийг нэхэмжлэлтэй холбов.',
    (SELECT id FROM users WHERE username = 'erp_admin_mn'), '2026-05-01 14:25:00', 0, null
WHERE NOT EXISTS (SELECT 1 FROM university_integration_runs WHERE integration_key = 'BANK_PAYMENT_SYNC' AND exchanged_at = '2026-05-01 14:25:00');

INSERT INTO university_integration_runs (
    integration_key, integration_name, direction, status, payload, result_message,
    actor_user_id, exchanged_at, retry_count, error_message
) SELECT 'GOV_STUDENT_REPORT', 'Боловсролын байгууллагын оюутны тайлан', 'OUTBOUND', 'FAILED',
    '{"reportMonth":"2026-05","students":3}',
    'Демо алдаа: туршилтын төгсгөлийн цэг түр боломжгүй.',
    (SELECT id FROM users WHERE username = 'erp_admin_mn'), '2026-05-04 09:00:00', 1, 'HTTP 503 service unavailable'
WHERE NOT EXISTS (SELECT 1 FROM university_integration_runs WHERE integration_key = 'GOV_STUDENT_REPORT' AND exchanged_at = '2026-05-04 09:00:00');

-- Reporting / BI snapshots.
INSERT INTO university_report_definitions (report_key, name, category, description, active)
VALUES
    ('ENROLLMENT_SUMMARY_MN', 'Элсэлт ба бүртгэлийн хураангуй', 'ACADEMIC', 'Элсэгч, оюутан, хичээл сонголтын нэгтгэл.', true),
    ('FINANCE_COLLECTION_MN', 'Сургалтын төлбөрийн төлөлтийн тайлан', 'FINANCE', 'Нэхэмжлэл, төлбөр, үлдэгдлийн тойм.', true),
    ('FACULTY_WORKLOAD_MN', 'Багшийн ажлын ачааллын тайлан', 'HR', 'Тэнхим, багш, кредитийн ачааллын нэгтгэл.', true)
ON CONFLICT (report_key) DO UPDATE SET
    name = EXCLUDED.name,
    category = EXCLUDED.category,
    description = EXCLUDED.description,
    active = EXCLUDED.active;

INSERT INTO university_report_runs (
    report_definition_id, status, filters, snapshot_payload, row_count, actor_user_id, generated_at
)
SELECT rd.id, 'COMPLETED',
    '{"academicYear":"2025-2026","semester":2}',
    '{"applicants":3,"students":3,"courseSelections":2,"acceptedApplicants":1}',
    3,
    (SELECT id FROM users WHERE username = 'erp_admin_mn'),
    '2026-05-04 10:00:00'
FROM university_report_definitions rd
WHERE rd.report_key = 'ENROLLMENT_SUMMARY_MN'
  AND NOT EXISTS (SELECT 1 FROM university_report_runs WHERE report_definition_id = rd.id AND generated_at = '2026-05-04 10:00:00');

INSERT INTO university_report_runs (
    report_definition_id, status, filters, snapshot_payload, row_count, actor_user_id, generated_at
)
SELECT rd.id, 'COMPLETED',
    '{"from":"2026-05-01","to":"2026-05-31"}',
    '{"issuedInvoices":2,"paidAmount":900000,"openAmount":1885000}',
    2,
    (SELECT id FROM users WHERE username = 'erp_admin_mn'),
    '2026-05-04 10:05:00'
FROM university_report_definitions rd
WHERE rd.report_key = 'FINANCE_COLLECTION_MN'
  AND NOT EXISTS (SELECT 1 FROM university_report_runs WHERE report_definition_id = rd.id AND generated_at = '2026-05-04 10:05:00');

-- Cross-module audit trail.
INSERT INTO university_erp_event_logs (module, action, entity_type, entity_id, actor_user_id, student_id, details)
SELECT 'ADMISSIONS', 'CONVERT_APPLICANT', 'university_applicants', a.id,
    (SELECT id FROM users WHERE username = 'erp_admin_mn'),
    (SELECT id FROM users WHERE username = 'erp_student_bat'),
    'Элсэгчийг оюутны бүртгэл рүү хөрвүүлэв.'
FROM university_applicants a
WHERE a.application_number = 'APP-MN-2026-001'
  AND NOT EXISTS (SELECT 1 FROM university_erp_event_logs WHERE module = 'ADMISSIONS' AND action = 'CONVERT_APPLICANT' AND entity_id = a.id);

INSERT INTO university_erp_event_logs (module, action, entity_type, entity_id, actor_user_id, student_id, details)
SELECT 'FINANCE', 'PAYMENT_MATCHED', 'fee_payments', p.id,
    (SELECT id FROM users WHERE username = 'erp_admin_mn'),
    p.student_id,
    'Банкны демо төлбөрийг нэхэмжлэлтэй холбов.'
FROM fee_payments p
WHERE p.reference_number = 'KHAN-DEMO-0001'
  AND NOT EXISTS (SELECT 1 FROM university_erp_event_logs WHERE module = 'FINANCE' AND action = 'PAYMENT_MATCHED' AND entity_id = p.id);

COMMIT;
