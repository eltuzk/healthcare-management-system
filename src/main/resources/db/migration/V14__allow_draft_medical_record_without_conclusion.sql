-- ============================================================
-- Allow draft medical records to be created before final conclusion
-- ============================================================

ALTER TABLE MEDICAL_RECORD MODIFY (
    conclusion_type NULL
);
