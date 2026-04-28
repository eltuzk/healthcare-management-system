-- ============================================================
-- Align DOCTOR.experience with application model
-- ============================================================

ALTER TABLE DOCTOR ADD (
    experience_text VARCHAR2(500)
);

UPDATE DOCTOR
SET experience_text = TO_CHAR(experience)
WHERE experience IS NOT NULL;

ALTER TABLE DOCTOR DROP COLUMN experience;

ALTER TABLE DOCTOR RENAME COLUMN experience_text TO experience;
