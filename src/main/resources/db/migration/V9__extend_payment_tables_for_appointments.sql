-- ============================================================
-- Extend shared payment tables for appointment payments
-- ============================================================

ALTER TABLE PAYMENT_RECORD MODIFY (
    med_record_id NULL
);

ALTER TABLE PAYMENT_RECORD ADD (
    appointment_id   NUMBER,
    received_amount  NUMBER(15, 2) DEFAULT 0 NOT NULL
);

ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT fk_payrecord_appointment
    FOREIGN KEY (appointment_id) REFERENCES APPOINTMENT(appointment_id);

ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT uq_payrecord_appointment
    UNIQUE (appointment_id);

ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT chk_payrecord_received
    CHECK (received_amount >= 0);

ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT chk_payrecord_owner
    CHECK (
        (med_record_id IS NOT NULL AND appointment_id IS NULL)
        OR (med_record_id IS NULL AND appointment_id IS NOT NULL)
    );

ALTER TABLE PAYMENT_TRANSACTION ADD CONSTRAINT uq_paytxn_sepay_tx
    UNIQUE (sepay_transaction_id);
