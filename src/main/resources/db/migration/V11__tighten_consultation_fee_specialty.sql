-- ============================================================
-- Tighten consultation fee specialty mapping
-- ============================================================

ALTER TABLE CONSULTATION_FEE MODIFY (
    specialty VARCHAR2(100) NOT NULL
);

ALTER TABLE CONSULTATION_FEE ADD CONSTRAINT uq_consultation_fee_specialty
    UNIQUE (specialty);
