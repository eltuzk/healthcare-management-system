-- ============================================================
-- Add specialty_id to ROOM table to support Clinic-Specialty mapping
-- ============================================================

ALTER TABLE ROOM ADD (
    specialty_id NUMBER
);

ALTER TABLE ROOM ADD CONSTRAINT fk_room_specialty 
    FOREIGN KEY (specialty_id) REFERENCES SPECIALTY(specialty_id);

CREATE INDEX idx_room_specialty ON ROOM(specialty_id);
