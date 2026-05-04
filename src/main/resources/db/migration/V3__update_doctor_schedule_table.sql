-- ============================================================
-- Update DOCTOR_SCHEDULE to fixed-shift model
-- ============================================================

ALTER TABLE DOCTOR_SCHEDULE ADD (
    shift      VARCHAR2(20),
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    updated_at TIMESTAMP
);

UPDATE DOCTOR_SCHEDULE
SET shift = CASE
    WHEN start_time = '07:00' AND end_time = '11:00' THEN 'MORNING'
    WHEN start_time = '13:00' AND end_time = '17:00' THEN 'AFTERNOON'
    ELSE 'MORNING'
END
WHERE shift IS NULL;

UPDATE DOCTOR_SCHEDULE
SET created_at = SYSTIMESTAMP
WHERE created_at IS NULL;

ALTER TABLE DOCTOR_SCHEDULE MODIFY (
    shift VARCHAR2(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE DOCTOR_SCHEDULE DROP CONSTRAINT uq_schedule_doctor_date;

ALTER TABLE DOCTOR_SCHEDULE ADD CONSTRAINT chk_schedule_shift
    CHECK (shift IN ('MORNING', 'AFTERNOON'));

ALTER TABLE DOCTOR_SCHEDULE ADD CONSTRAINT chk_schedule_queue
    CHECK (last_queue_number >= 0);

ALTER TABLE DOCTOR_SCHEDULE ADD CONSTRAINT uq_schedule_doctor_shift
    UNIQUE (doctor_id, schedule_date, shift);

ALTER TABLE DOCTOR_SCHEDULE ADD CONSTRAINT uq_schedule_room_shift
    UNIQUE (room_id, schedule_date, shift);

ALTER TABLE DOCTOR_SCHEDULE DROP COLUMN start_time;

ALTER TABLE DOCTOR_SCHEDULE DROP COLUMN end_time;

CREATE INDEX idx_doctor_schedule_date
    ON DOCTOR_SCHEDULE (schedule_date);
