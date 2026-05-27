-- ============================================================
-- Remove unique constraints on consultation fee specialty mapping
-- ============================================================

ALTER TABLE CONSULTATION_FEE DROP CONSTRAINT uq_consultation_fee_specialty;
ALTER TABLE CONSULTATION_FEE DROP CONSTRAINT uq_consultation_fee_specialty_id;
