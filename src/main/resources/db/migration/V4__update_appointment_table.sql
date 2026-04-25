-- ============================================================
-- Update APPOINTMENT for phase 1 lifecycle flow
-- ============================================================

ALTER TABLE APPOINTMENT MODIFY (
    queue_num NULL
);

ALTER TABLE APPOINTMENT ADD (
    initial_symptoms CLOB,
    visit_reason VARCHAR2(500),
    updated_at TIMESTAMP,
    checked_in_at TIMESTAMP,
    cancelled_at TIMESTAMP
);

ALTER TABLE APPOINTMENT DROP CONSTRAINT chk_appt_status;

ALTER TABLE APPOINTMENT ADD CONSTRAINT chk_appt_status
    CHECK (status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'));

ALTER TABLE APPOINTMENT DROP CONSTRAINT uq_appt_patient_schedule;

CREATE INDEX idx_appt_patient_status
    ON APPOINTMENT (patient_id, status);
