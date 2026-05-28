CREATE OR REPLACE PROCEDURE SP_CREATE_LAB_TEST_REQUEST (
    p_med_record_id IN NUMBER,
    p_lab_test_ids_csv IN VARCHAR2,
    p_note IN VARCHAR2,
    o_lab_test_request_id OUT NUMBER,
    o_request_code OUT VARCHAR2
) IS
    v_total_price NUMBER(15,2) := 0;
    v_snapshot_price NUMBER(15,2);
BEGIN
    o_request_code := 'LTR-' || SUBSTR(RAWTOHEX(SYS_GUID()), 1, 8);

    INSERT INTO LAB_TEST_REQUEST (
        med_record_id, request_code, status, payment_status, total_price, note, created_at
    ) VALUES (
        p_med_record_id, o_request_code, 'PENDING', 'UNPAID', 0, p_note, SYSTIMESTAMP
    ) RETURNING lab_test_request_id INTO o_lab_test_request_id;

    FOR req_item IN (
        SELECT TO_NUMBER(REGEXP_SUBSTR(p_lab_test_ids_csv, '[^,]+', 1, LEVEL)) AS l_id
        FROM dual
        CONNECT BY REGEXP_SUBSTR(p_lab_test_ids_csv, '[^,]+', 1, LEVEL) IS NOT NULL
    )
    LOOP
        SELECT price INTO v_snapshot_price 
        FROM LAB_TEST 
        WHERE lab_test_id = req_item.l_id AND is_active = 1;

        INSERT INTO LAB_TEST_REQUEST_ITEM (
            lab_test_request_id, lab_test_id, snapshot_price
        ) VALUES (
            o_lab_test_request_id, req_item.l_id, v_snapshot_price
        );

        v_total_price := v_total_price + v_snapshot_price;
    END LOOP;

    UPDATE LAB_TEST_REQUEST
    SET total_price = v_total_price
    WHERE lab_test_request_id = o_lab_test_request_id;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END SP_CREATE_LAB_TEST_REQUEST;
/
