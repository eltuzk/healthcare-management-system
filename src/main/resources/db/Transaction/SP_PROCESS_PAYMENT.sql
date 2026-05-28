CREATE OR REPLACE PROCEDURE SP_PROCESS_PAYMENT (
    p_payment_record_id IN NUMBER,
    p_transfer_amount IN NUMBER,
    p_gateway IN VARCHAR2,
    p_transfer_type IN VARCHAR2,
    p_sepay_transaction_id IN VARCHAR2,
    p_reference_code IN VARCHAR2,
    p_content IN VARCHAR2,
    o_transaction_id OUT NUMBER
) IS
    v_total_price NUMBER(15,2);
    v_current_received NUMBER(15,2);
    v_new_received NUMBER(15,2);
    v_status VARCHAR2(20);
BEGIN
    SELECT total_price, received_amount
    INTO v_total_price, v_current_received
    FROM PAYMENT_RECORD
    WHERE payment_record_id = p_payment_record_id
    FOR UPDATE;

    v_new_received := v_current_received + p_transfer_amount;
    
    IF v_new_received >= v_total_price THEN
        v_status := 'PAID';
    ELSIF v_new_received > 0 THEN
        v_status := 'PARTIAL';
    ELSE
        v_status := 'UNPAID';
    END IF;

    UPDATE PAYMENT_RECORD
    SET received_amount = v_new_received,
        payment_status = v_status,
        paid_at = CASE WHEN v_status = 'PAID' THEN SYSTIMESTAMP ELSE paid_at END
    WHERE payment_record_id = p_payment_record_id;

    INSERT INTO PAYMENT_TRANSACTION (
        payment_record_id, transfer_type, gateway, transfer_amount, transaction_date, 
        sepay_transaction_id, reference_code, content, process_status
    ) VALUES (
        p_payment_record_id, p_transfer_type, p_gateway, p_transfer_amount, SYSTIMESTAMP,
        p_sepay_transaction_id, p_reference_code, p_content, 'SUCCESS'
    ) RETURNING transaction_id INTO o_transaction_id;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END SP_PROCESS_PAYMENT;
/
