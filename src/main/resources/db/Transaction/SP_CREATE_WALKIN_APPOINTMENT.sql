CREATE OR REPLACE PROCEDURE SP_CREATE_WALKIN_APPOINTMENT (
    p_patient_id IN NUMBER,
    p_doctor_schedule_id IN NUMBER,
    p_fee_id IN NUMBER,
    p_fee_name_snapshot IN VARCHAR2,
    p_fee_price_snapshot IN NUMBER,
    p_initial_symptoms IN CLOB,
    p_visit_reason IN VARCHAR2,
    p_received_amount IN NUMBER,
    p_receipt_number IN VARCHAR2,
    p_note IN VARCHAR2,
    p_confirmed_by_account_id IN NUMBER,
    o_appointment_id OUT NUMBER,
    o_appointment_code OUT VARCHAR2,
    o_payment_record_id OUT NUMBER
) IS
    v_next_queue_num NUMBER;
    v_capacity NUMBER;
    v_current_booking NUMBER;
BEGIN
    IF p_received_amount <> p_fee_price_snapshot THEN
        RAISE_APPLICATION_ERROR(-20002, 'Received amount must match consultation fee amount');
    END IF;

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
        fee_id, fee_name_snapshot, fee_price_snapshot, initial_symptoms, visit_reason, created_at, checked_in_at
    ) VALUES (
        p_patient_id, p_doctor_schedule_id, v_next_queue_num, 'CHECKED_IN', o_appointment_code,
        p_fee_id, p_fee_name_snapshot, p_fee_price_snapshot, p_initial_symptoms, p_visit_reason, SYSTIMESTAMP, SYSTIMESTAMP
    ) RETURNING appointment_id INTO o_appointment_id;

    INSERT INTO PAYMENT_RECORD (
        appointment_id, request_code, total_price, received_amount, payment_status, created_at, paid_at
    ) VALUES (
        o_appointment_id, o_appointment_code, p_fee_price_snapshot, p_received_amount, 'PAID', SYSTIMESTAMP, SYSTIMESTAMP
    ) RETURNING payment_record_id INTO o_payment_record_id;

    INSERT INTO PAYMENT_TRANSACTION (
        payment_record_id, transfer_type, gateway, transfer_amount, transaction_date, content, description, process_status, receipt_number, confirmed_by_account_id
    ) VALUES (
        o_payment_record_id, 'cash', 'CASH', p_received_amount, SYSTIMESTAMP, p_note, p_note, 'SUCCESS', p_receipt_number, p_confirmed_by_account_id
    );

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END SP_CREATE_WALKIN_APPOINTMENT;
/
