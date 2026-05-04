-- ============================================================
-- Restructure MEDICAL_RECORD for clinical workflow
-- ============================================================

ALTER TABLE MEDICAL_RECORD RENAME COLUMN conclusion TO clinical_conclusion;

ALTER TABLE MEDICAL_RECORD ADD (
    doctor_id NUMBER,
    patient_id NUMBER,
    conclusion_type VARCHAR2(30),
    clinical_notes CLOB,
    treatment_plan CLOB,
    status VARCHAR2(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP,
    locked_at TIMESTAMP,
    version_number NUMBER(19) DEFAULT 0 NOT NULL
);

UPDATE MEDICAL_RECORD mr
SET patient_id = (
        SELECT a.patient_id
        FROM APPOINTMENT a
        WHERE a.appointment_id = mr.appointment_id
    ),
    doctor_id = (
        SELECT ds.doctor_id
        FROM APPOINTMENT a
        JOIN DOCTOR_SCHEDULE ds ON ds.doctor_schedule_id = a.doctor_schedule_id
        WHERE a.appointment_id = mr.appointment_id
    ),
    conclusion_type = 'COMPLETED',
    status = 'COMPLETED',
    created_at = COALESCE(CAST(record_date AS TIMESTAMP), SYSTIMESTAMP),
    completed_at = COALESCE(CAST(record_date AS TIMESTAMP), SYSTIMESTAMP);

ALTER TABLE MEDICAL_RECORD MODIFY (
    record_date NULL,
    doctor_id NOT NULL,
    patient_id NOT NULL,
    conclusion_type VARCHAR2(30) NOT NULL,
    status VARCHAR2(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE MEDICAL_RECORD ADD CONSTRAINT fk_medrecord_doctor
    FOREIGN KEY (doctor_id) REFERENCES DOCTOR(doctor_id);

ALTER TABLE MEDICAL_RECORD ADD CONSTRAINT fk_medrecord_patient
    FOREIGN KEY (patient_id) REFERENCES PATIENT(patient_id);

ALTER TABLE MEDICAL_RECORD ADD CONSTRAINT chk_medrecord_status
    CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'COMPLETED', 'LOCKED'));

ALTER TABLE MEDICAL_RECORD ADD CONSTRAINT chk_medrecord_conclusion_type
    CHECK (conclusion_type IN ('COMPLETED', 'ADMISSION_REQUIRED'));

CREATE INDEX idx_medrecord_patient_status
    ON MEDICAL_RECORD (patient_id, status);

CREATE INDEX idx_medrecord_doctor_date
    ON MEDICAL_RECORD (doctor_id, created_at);
