CREATE OR REPLACE PROCEDURE SP_CREATE_APPOINTMENT (
    p_patient_id IN NUMBER,
    p_doctor_schedule_id IN NUMBER,
    p_fee_id IN NUMBER,
    p_fee_name_snapshot IN VARCHAR2,
    p_fee_price_snapshot IN NUMBER,
    p_initial_symptoms IN CLOB,
    p_visit_reason IN VARCHAR2,
    o_appointment_id OUT NUMBER,
    o_appointment_code OUT VARCHAR2
) IS
    v_next_queue_num NUMBER;
    v_capacity NUMBER;
    v_current_booking NUMBER;
BEGIN
    SELECT max_capacity, current_booking_count, last_queue_number
    INTO v_capacity, v_current_booking, v_next_queue_num
    FROM DOCTOR_SCHEDULE
    WHERE doctor_schedule_id = p_doctor_schedule_id
    FOR UPDATE;

    IF v_current_booking >= v_capacity THEN
        RAISE_APPLICATION_ERROR(-20001, 'Doctor schedule is full');
    END IF;

    v_next_queue_num := v_next_queue_num + 1;
    v_current_booking := v_current_booking + 1;

    UPDATE DOCTOR_SCHEDULE
    SET last_queue_number = v_next_queue_num,
        current_booking_count = v_current_booking
    WHERE doctor_schedule_id = p_doctor_schedule_id;

    o_appointment_code := 'APT-' || SUBSTR(RAWTOHEX(SYS_GUID()), 1, 8);

    INSERT INTO APPOINTMENT (
        patient_id, doctor_schedule_id, queue_num, status, appointment_code,
        fee_id, fee_name_snapshot, fee_price_snapshot, initial_symptoms, visit_reason, created_at
    ) VALUES (
        p_patient_id, p_doctor_schedule_id, v_next_queue_num, 'PENDING', o_appointment_code,
        p_fee_id, p_fee_name_snapshot, p_fee_price_snapshot, p_initial_symptoms, p_visit_reason, SYSTIMESTAMP
    ) RETURNING appointment_id INTO o_appointment_id;

    INSERT INTO PAYMENT_RECORD (
        appointment_id, request_code, total_price, payment_status, created_at
    ) VALUES (
        o_appointment_id, o_appointment_code, p_fee_price_snapshot, 'UNPAID', SYSTIMESTAMP
    );

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END SP_CREATE_APPOINTMENT;
/
