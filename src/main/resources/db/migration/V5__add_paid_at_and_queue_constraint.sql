-- ============================================================
-- Add paid_at and queue uniqueness protection for appointments
-- ============================================================

ALTER TABLE APPOINTMENT ADD (
    paid_at TIMESTAMP
);

ALTER TABLE APPOINTMENT ADD CONSTRAINT uq_appt_schedule_queue
    UNIQUE (doctor_schedule_id, queue_num);
