CREATE OR REPLACE PROCEDURE SP_CANCEL_APPOINTMENT (
    p_appointment_id IN NUMBER
) IS
    v_status VARCHAR2(20);
    v_doctor_schedule_id NUMBER;
BEGIN
    SELECT status, doctor_schedule_id 
    INTO v_status, v_doctor_schedule_id
    FROM APPOINTMENT
    WHERE appointment_id = p_appointment_id
    FOR UPDATE;

    IF v_status IN ('CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED') THEN
        RAISE_APPLICATION_ERROR(-20003, 'Paid appointments cannot be cancelled');
    END IF;

    IF v_status = 'CANCELLED' THEN
        RAISE_APPLICATION_ERROR(-20004, 'Appointment is already cancelled');
    END IF;

    UPDATE APPOINTMENT 
    SET status = 'CANCELLED',
        cancelled_at = SYSTIMESTAMP
    WHERE appointment_id = p_appointment_id;

    UPDATE DOCTOR_SCHEDULE
    SET current_booking_count = GREATEST(current_booking_count - 1, 0)
    WHERE doctor_schedule_id = v_doctor_schedule_id;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END SP_CANCEL_APPOINTMENT;
/
