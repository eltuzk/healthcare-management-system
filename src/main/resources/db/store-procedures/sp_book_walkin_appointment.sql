CREATE OR REPLACE PROCEDURE sp_book_walkin_appointment (
    p_patient_id IN NUMBER,
    p_doctor_schedule_id IN NUMBER,
    p_initial_symptoms IN CLOB,
    o_appointment_id OUT NUMBER
)
IS
    v_doctor_id NUMBER;
    v_specialty_id NUMBER;
    v_fee_id NUMBER;
    v_fee_name VARCHAR2(200);
    v_fee_price NUMBER(15,2);
    v_queue_num NUMBER;
    v_appt_code VARCHAR2(30);
    v_payment_record_id NUMBER;
BEGIN
    -- 1. Get schedule info
    SELECT doctor_id INTO v_doctor_id
    FROM DOCTOR_SCHEDULE
    WHERE doctor_schedule_id = p_doctor_schedule_id
    FOR UPDATE; -- Lock row

    -- 2. Get Fee based on doctor specialty
    SELECT cf.fee_id, cf.fee_name, cf.price
    INTO v_fee_id, v_fee_name, v_fee_price
    FROM DOCTOR d
    JOIN CONSULTATION_FEE cf ON d.specialty_id = cf.specialty_id
    WHERE d.doctor_id = v_doctor_id AND cf.is_active = 1 AND ROWNUM = 1;

    -- 3. Generate Appt Code
    v_appt_code := fn_generate_appointment_code();

    -- 4. Get Queue Number
    v_queue_num := fn_get_next_queue_number(p_doctor_schedule_id);

    -- 5. Insert Appointment
    INSERT INTO APPOINTMENT (
        appointment_code, patient_id, doctor_schedule_id, fee_id,
        fee_name_snapshot, fee_price_snapshot, queue_num, status,
        initial_symptoms, created_at, checked_in_at, version_number
    ) VALUES (
        v_appt_code, p_patient_id, p_doctor_schedule_id, v_fee_id,
        v_fee_name, v_fee_price, v_queue_num, 'CHECKED_IN',
        p_initial_symptoms, SYSTIMESTAMP, SYSTIMESTAMP, 0
    ) RETURNING appointment_id INTO o_appointment_id;

    -- 6. Update Schedule
    UPDATE DOCTOR_SCHEDULE
    SET current_booking_count = current_booking_count + 1,
        last_queue_number = v_queue_num
    WHERE doctor_schedule_id = p_doctor_schedule_id;

    -- 7. Insert Payment Record
    INSERT INTO PAYMENT_RECORD (
        appointment_id, request_code, total_price, received_amount,
        payment_status, created_at
    ) VALUES (
        o_appointment_id, 'PR-' || v_appt_code, v_fee_price, 0,
        'UNPAID', SYSTIMESTAMP
    ) RETURNING payment_record_id INTO v_payment_record_id;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
