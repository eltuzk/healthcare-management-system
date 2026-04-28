-- ============================================================
-- Add optimistic locking columns for shared write entities
-- ============================================================

ALTER TABLE DOCTOR_SCHEDULE ADD (
    version_number NUMBER(19) DEFAULT 0 NOT NULL
);

ALTER TABLE APPOINTMENT ADD (
    version_number NUMBER(19) DEFAULT 0 NOT NULL
);
