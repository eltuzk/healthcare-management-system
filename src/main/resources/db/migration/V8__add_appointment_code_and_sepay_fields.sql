-- ============================================================
-- Add appointment payment code and SePay webhook tracking fields
-- ============================================================

ALTER TABLE APPOINTMENT ADD (
    appointment_code VARCHAR2(30),
    sepay_transaction_id NUMBER(19),
    payment_reference_code VARCHAR2(200),
    payment_content VARCHAR2(1000)
);

UPDATE APPOINTMENT
SET appointment_code = 'APT-' || LPAD(TO_CHAR(appointment_id), 8, '0')
WHERE appointment_code IS NULL;

ALTER TABLE APPOINTMENT MODIFY (
    appointment_code VARCHAR2(30) NOT NULL
);

ALTER TABLE APPOINTMENT ADD CONSTRAINT uq_appt_code
    UNIQUE (appointment_code);

ALTER TABLE APPOINTMENT ADD CONSTRAINT uq_appt_sepay_tx
    UNIQUE (sepay_transaction_id);
