-- V3: Rental hold tracking (pay-first + concurrency) and saved payment methods.

-- Rental: track creation time (for hold expiry) and payment status (for tracking display).
ALTER TABLE rentals ADD COLUMN IF NOT EXISTS created_at TIMESTAMP(6);
ALTER TABLE rentals ADD COLUMN IF NOT EXISTS payment_status VARCHAR(255);

-- Backfill existing rows so the new columns are populated.
UPDATE rentals SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE rentals SET payment_status = 'PENDING' WHERE payment_status IS NULL;

-- Saved payment methods. Metadata only — never store raw card numbers / CVV / credentials.
CREATE TABLE IF NOT EXISTS payment_methods (
    payment_method_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,            -- MOMO, BANK
    label VARCHAR(255) NOT NULL,          -- display name, e.g. "Vietcombank" or "Vi MoMo"
    account_holder VARCHAR(255),          -- name on the account
    masked_account VARCHAR(255),          -- masked identifier, e.g. "**** 1234" or "09xx xxx 678"
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_payment_methods_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_payment_methods_user ON payment_methods(user_id);
