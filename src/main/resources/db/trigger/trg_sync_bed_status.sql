CREATE OR REPLACE TRIGGER trg_sync_bed_status
AFTER UPDATE ON ADMISSION_REQUEST
FOR EACH ROW
BEGIN
    IF :OLD.status != :NEW.status THEN
        IF :NEW.status = 'ADMITTED' THEN
            UPDATE BED SET status = 'OCCUPIED' WHERE bed_id = :NEW.bed_id;
        ELSIF :NEW.status IN ('DISCHARGED', 'CANCELLED') THEN
            UPDATE BED SET status = 'AVAILABLE' WHERE bed_id = :NEW.bed_id;
        END IF;
    END IF;
END;
/
