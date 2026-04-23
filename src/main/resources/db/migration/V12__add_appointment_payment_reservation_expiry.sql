-- ============================================================
-- Add payment reservation expiry for online appointment booking
-- ============================================================

ALTER TABLE APPOINTMENT ADD (
    payment_expires_at TIMESTAMP
);

ALTER TABLE APPOINTMENT DROP CONSTRAINT chk_appt_status;

ALTER TABLE APPOINTMENT ADD CONSTRAINT chk_appt_status
    CHECK (status IN (
        'PENDING',
        'CONFIRMED',
        'CHECKED_IN',
        'IN_PROGRESS',
        'COMPLETED',
        'CANCELLED',
        'PAYMENT_EXPIRED'
    ));

CREATE INDEX idx_appt_payment_expiry
    ON APPOINTMENT (status, payment_expires_at);
