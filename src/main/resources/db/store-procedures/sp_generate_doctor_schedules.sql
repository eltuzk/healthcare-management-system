CREATE OR REPLACE PROCEDURE sp_generate_doctor_schedules (
    p_doctor_id IN NUMBER,
    p_room_id IN NUMBER,
    p_start_date IN DATE,
    p_end_date IN DATE,
    p_max_capacity IN NUMBER
)
IS
    v_curr_date DATE := TRUNC(p_start_date);
    v_end_date DATE := TRUNC(p_end_date);
BEGIN
    WHILE v_curr_date <= v_end_date LOOP
        -- Insert Morning Shift
        INSERT INTO DOCTOR_SCHEDULE (
            doctor_id, room_id, schedule_date, shift, max_capacity, 
            current_booking_count, last_queue_number, created_at, version_number
        ) VALUES (
            p_doctor_id, p_room_id, v_curr_date, 'MORNING', p_max_capacity,
            0, 0, SYSTIMESTAMP, 0
        );
        
        -- Insert Afternoon Shift
        INSERT INTO DOCTOR_SCHEDULE (
            doctor_id, room_id, schedule_date, shift, max_capacity, 
            current_booking_count, last_queue_number, created_at, version_number
        ) VALUES (
            p_doctor_id, p_room_id, v_curr_date, 'AFTERNOON', p_max_capacity,
            0, 0, SYSTIMESTAMP, 0
        );
        
        v_curr_date := v_curr_date + 1;
    END LOOP;
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
