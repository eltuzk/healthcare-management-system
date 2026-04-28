-- ============================================================
-- Add specialty master data and replace text-based fee mapping
-- ============================================================

CREATE TABLE SPECIALTY (
    specialty_id    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    specialty_code  VARCHAR2(50)  NOT NULL UNIQUE,
    specialty_name  VARCHAR2(200) NOT NULL UNIQUE,
    is_active       NUMBER(1) DEFAULT 1 NOT NULL,
    created_at      TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at      TIMESTAMP,
    CONSTRAINT chk_specialty_active CHECK (is_active IN (0, 1))
);

INSERT INTO SPECIALTY (specialty_code, specialty_name)
SELECT
    'SPEC-' || LPAD(ROW_NUMBER() OVER (ORDER BY specialty_name), 4, '0') AS specialty_code,
    specialty_name
FROM (
    SELECT MIN(specialty_name) AS specialty_name
    FROM (
        SELECT TRIM(specialization) AS specialty_name
        FROM DOCTOR
        WHERE specialization IS NOT NULL AND TRIM(specialization) IS NOT NULL
        UNION ALL
        SELECT TRIM(specialty) AS specialty_name
        FROM CONSULTATION_FEE
        WHERE specialty IS NOT NULL AND TRIM(specialty) IS NOT NULL
    )
    GROUP BY UPPER(specialty_name)
);

ALTER TABLE DOCTOR ADD (
    specialty_id NUMBER
);

ALTER TABLE CONSULTATION_FEE ADD (
    specialty_id NUMBER
);

UPDATE DOCTOR d
SET specialty_id = (
    SELECT s.specialty_id
    FROM SPECIALTY s
    WHERE UPPER(s.specialty_name) = UPPER(TRIM(d.specialization))
)
WHERE d.specialization IS NOT NULL;

UPDATE CONSULTATION_FEE cf
SET specialty_id = (
    SELECT s.specialty_id
    FROM SPECIALTY s
    WHERE UPPER(s.specialty_name) = UPPER(TRIM(cf.specialty))
)
WHERE cf.specialty IS NOT NULL;

ALTER TABLE DOCTOR ADD CONSTRAINT fk_doctor_specialty
    FOREIGN KEY (specialty_id) REFERENCES SPECIALTY(specialty_id);

ALTER TABLE CONSULTATION_FEE ADD CONSTRAINT fk_consultation_fee_specialty
    FOREIGN KEY (specialty_id) REFERENCES SPECIALTY(specialty_id);

ALTER TABLE CONSULTATION_FEE ADD CONSTRAINT uq_consultation_fee_specialty_id
    UNIQUE (specialty_id);

CREATE INDEX idx_doctor_specialty
    ON DOCTOR (specialty_id);
