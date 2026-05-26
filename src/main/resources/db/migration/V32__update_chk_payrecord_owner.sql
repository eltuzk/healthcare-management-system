-- ============================================================
-- Update chk_payrecord_owner check constraint to support
-- prescription-based payment records.
-- ============================================================

ALTER TABLE PAYMENT_RECORD DROP CONSTRAINT chk_payrecord_owner;

ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT chk_payrecord_owner
    CHECK (
        (med_record_id IS NOT NULL AND appointment_id IS NULL AND prescription_id IS NULL)
        OR (med_record_id IS NULL AND appointment_id IS NOT NULL AND prescription_id IS NULL)
        OR (med_record_id IS NULL AND appointment_id IS NULL AND prescription_id IS NOT NULL)
    );
