CREATE OR REPLACE TRIGGER trg_prevent_cancel_paid_appt
BEFORE UPDATE OR DELETE ON APPOINTMENT
FOR EACH ROW
BEGIN
    IF DELETING THEN
        IF :OLD.status IN ('CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED') OR :OLD.paid_at IS NOT NULL THEN
            RAISE_APPLICATION_ERROR(-20001, 'Không thể xóa lịch khám đã thanh toán hoặc đã xác nhận.');
        END IF;
    ELSIF UPDATING THEN
        IF :NEW.status = 'CANCELLED' AND (:OLD.status IN ('CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED') OR :OLD.paid_at IS NOT NULL) THEN
            RAISE_APPLICATION_ERROR(-20002, 'Không thể hủy (CANCEL) lịch khám đã thanh toán hoặc đang tiến hành.');
        END IF;
    END IF;
END;
/
