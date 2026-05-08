CREATE OR REPLACE FUNCTION fn_check_doctor_availability (
    p_doctor_id IN NUMBER,
    p_schedule_date IN DATE,
    p_shift IN VARCHAR2
) RETURN NUMBER
IS
    v_available NUMBER := 0;
    v_max_capacity NUMBER;
    v_current_booking NUMBER;
BEGIN
    SELECT max_capacity, current_booking_count
    INTO v_max_capacity, v_current_booking
    FROM DOCTOR_SCHEDULE
    WHERE doctor_id = p_doctor_id
      AND schedule_date = p_schedule_date
      AND shift = p_shift;
      
    IF v_current_booking < v_max_capacity THEN
        v_available := 1;
    END IF;
    
    RETURN v_available;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN 0;
    WHEN OTHERS THEN
        RETURN 0;
END;
/
