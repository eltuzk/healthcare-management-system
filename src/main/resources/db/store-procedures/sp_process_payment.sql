CREATE OR REPLACE PROCEDURE sp_process_payment (
    p_payment_record_id IN NUMBER,
    p_gateway IN VARCHAR2,
    p_transfer_amount IN NUMBER,
    p_reference_code IN VARCHAR2,
    p_account_id IN NUMBER
)
IS
    v_total_price NUMBER(15,2);
    v_received NUMBER(15,2);
    v_appt_id NUMBER;
    v_med_record_id NUMBER;
    v_new_status VARCHAR2(20);
BEGIN
    -- 1. Insert Transaction
    INSERT INTO PAYMENT_TRANSACTION (
        payment_record_id, gateway, transfer_amount, transaction_date, 
        reference_code, process_status, confirmed_by_account_id
    ) VALUES (
        p_payment_record_id, p_gateway, p_transfer_amount, SYSTIMESTAMP,
        p_reference_code, 'SUCCESS', p_account_id
    );

    -- 2. Update Payment Record
    SELECT total_price, received_amount, appointment_id, med_record_id
    INTO v_total_price, v_received, v_appt_id, v_med_record_id
    FROM PAYMENT_RECORD
    WHERE payment_record_id = p_payment_record_id
    FOR UPDATE;

    v_received := v_received + p_transfer_amount;
    
    IF v_received >= v_total_price THEN
        v_new_status := 'PAID';
    ELSE
        v_new_status := 'PARTIAL';
    END IF;

    UPDATE PAYMENT_RECORD
    SET received_amount = v_received,
        payment_status = v_new_status,
        updated_at = SYSTIMESTAMP,
        paid_at = CASE WHEN v_new_status = 'PAID' THEN SYSTIMESTAMP ELSE paid_at END
    WHERE payment_record_id = p_payment_record_id;

    -- 3. Update Appointment if PAID
    IF v_new_status = 'PAID' AND v_appt_id IS NOT NULL THEN
        UPDATE APPOINTMENT
        SET status = CASE WHEN status = 'PENDING' THEN 'CONFIRMED' ELSE status END,
            paid_at = SYSTIMESTAMP
        WHERE appointment_id = v_appt_id;
    END IF;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
