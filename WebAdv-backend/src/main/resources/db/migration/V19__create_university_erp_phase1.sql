CREATE TABLE university_applicants (
    id BIGSERIAL PRIMARY KEY,
    application_number VARCHAR(40) UNIQUE NOT NULL,
    first_name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(60),
    program VARCHAR(160) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED' CHECK (status IN ('SUBMITTED','SCREENING','ACCEPTED','REJECTED','CONVERTED')),
    decision_notes TEXT,
    converted_student_id BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE university_course_selections (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    academic_year VARCHAR(20) NOT NULL,
    semester INTEGER NOT NULL,
    credits INTEGER NOT NULL CHECK (credits > 0),
    status VARCHAR(30) NOT NULL DEFAULT 'SELECTED' CHECK (status IN ('SELECTED','DROPPED','BILLED')),
    invoice_id BIGINT REFERENCES fee_invoices(id) ON UPDATE CASCADE ON DELETE SET NULL,
    selected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_university_course_selection UNIQUE (student_id, subject_id, academic_year, semester)
);

CREATE INDEX idx_university_applicants_status ON university_applicants(status);
CREATE INDEX idx_university_course_selections_student ON university_course_selections(student_id);
CREATE INDEX idx_university_course_selections_invoice ON university_course_selections(invoice_id);
