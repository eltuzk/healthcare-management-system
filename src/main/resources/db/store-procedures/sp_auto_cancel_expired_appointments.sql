CREATE OR REPLACE PROCEDURE sp_auto_cancel_expired_appointments
IS
    CURSOR c_expired IS
        SELECT appointment_id, doctor_schedule_id 
        FROM APPOINTMENT 
        WHERE status = 'PENDING' 
          AND payment_expires_at < SYSTIMESTAMP;
BEGIN
    FOR rec IN c_expired LOOP
        -- Update appointment status
        UPDATE APPOINTMENT
        SET status = 'PAYMENT_EXPIRED',
            cancelled_at = SYSTIMESTAMP
        WHERE appointment_id = rec.appointment_id;
        
        -- Decrease booking count
        UPDATE DOCTOR_SCHEDULE
        SET current_booking_count = GREATEST(current_booking_count - 1, 0)
        WHERE doctor_schedule_id = rec.doctor_schedule_id;
    END LOOP;
    
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
