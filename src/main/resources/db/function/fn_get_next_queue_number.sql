CREATE OR REPLACE FUNCTION fn_get_next_queue_number (
    p_doctor_schedule_id IN NUMBER
) RETURN NUMBER
IS
    v_next_queue NUMBER;
BEGIN
    SELECT NVL(last_queue_number, 0) + 1
    INTO v_next_queue
    FROM DOCTOR_SCHEDULE
    WHERE doctor_schedule_id = p_doctor_schedule_id;
    
    RETURN v_next_queue;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 1;
    WHEN OTHERS THEN
        RETURN -1;
END;
/
