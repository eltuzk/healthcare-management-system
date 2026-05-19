CREATE OR REPLACE TRIGGER trg_prevent_locked_mr_update
BEFORE UPDATE OR DELETE ON MEDICAL_RECORD
FOR EACH ROW
BEGIN
    IF :OLD.status = 'LOCKED' THEN
        IF DELETING THEN
            RAISE_APPLICATION_ERROR(-20003, 'Không thể xóa bệnh án đã khóa (LOCKED).');
        ELSIF UPDATING THEN
            IF :NEW.status = 'LOCKED' THEN 
                RAISE_APPLICATION_ERROR(-20004, 'Không thể chỉnh sửa bệnh án đã bị khóa (LOCKED).');
            END IF;
        END IF;
    END IF;
END;
/
