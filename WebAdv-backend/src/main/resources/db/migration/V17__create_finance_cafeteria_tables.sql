CREATE TABLE fee_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    category VARCHAR(30) NOT NULL CHECK (category IN ('TUITION','ACTIVITY','TRANSPORT','CAFETERIA','OTHER')),
    amount NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fee_invoices (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    invoice_number VARCHAR(40) UNIQUE NOT NULL,
    due_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'ISSUED' CHECK (status IN ('DRAFT','ISSUED','PARTIALLY_PAID','PAID','WAIVED','OVERDUE')),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fee_invoice_lines (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES fee_invoices(id) ON UPDATE CASCADE ON DELETE CASCADE,
    fee_item_id BIGINT REFERENCES fee_items(id) ON UPDATE CASCADE ON DELETE SET NULL,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount >= 0)
);

CREATE TABLE fee_payments (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES fee_invoices(id) ON UPDATE CASCADE ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    payment_date DATE NOT NULL,
    method VARCHAR(30) NOT NULL CHECK (method IN ('CASH','CARD','BANK_TRANSFER','ONLINE','OTHER')),
    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('PENDING','COMPLETED','FAILED','REFUNDED')),
    reference_number VARCHAR(100),
    notes TEXT,
    recorded_by BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE meal_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    price_per_meal NUMERIC(12, 2) NOT NULL CHECK (price_per_meal >= 0),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE meal_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    meal_type VARCHAR(30) NOT NULL CHECK (meal_type IN ('BREAKFAST','LUNCH','SNACK','DRINK','OTHER')),
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE meal_purchases (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE,
    meal_item_id BIGINT NOT NULL REFERENCES meal_items(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    meal_plan_id BIGINT REFERENCES meal_plans(id) ON UPDATE CASCADE ON DELETE SET NULL,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    total_amount NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
    purchase_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SERVED' CHECK (status IN ('RESERVED','SERVED','CANCELLED')),
    notes TEXT,
    recorded_by BIGINT REFERENCES users(id) ON UPDATE CASCADE ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fee_invoices_student ON fee_invoices(student_id);
CREATE INDEX idx_fee_payments_student ON fee_payments(student_id);
CREATE INDEX idx_meal_purchases_student ON meal_purchases(student_id);
