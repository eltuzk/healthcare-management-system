CREATE OR REPLACE FUNCTION fn_calculate_total_fee (
    p_med_record_id IN NUMBER
) RETURN NUMBER
IS
    v_total_fee NUMBER(15,2) := 0;
    v_appt_fee NUMBER(15,2) := 0;
    v_lab_fee NUMBER(15,2) := 0;
    v_service_fee NUMBER(15,2) := 0;
    v_prescription_fee NUMBER(15,2) := 0;
    v_admission_fee NUMBER(15,2) := 0;
BEGIN
    -- 1. Get Appointment Fee (from Medical Record -> Appointment)
    BEGIN
        SELECT NVL(a.fee_price_snapshot, 0)
        INTO v_appt_fee
        FROM MEDICAL_RECORD mr
        JOIN APPOINTMENT a ON mr.appointment_id = a.appointment_id
        WHERE mr.med_record_id = p_med_record_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_appt_fee := 0;
    END;

    -- 2. Get Lab Test Fee
    BEGIN
        SELECT NVL(SUM(total_price), 0)
        INTO v_lab_fee
        FROM LAB_TEST_REQUEST
        WHERE med_record_id = p_med_record_id
          AND status != 'CANCELLED';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_lab_fee := 0;
    END;

    -- 3. Get Medical Service Fee
    BEGIN
        SELECT NVL(SUM(total_price), 0)
        INTO v_service_fee
        FROM MEDICAL_SERVICE_REQUEST
        WHERE med_record_id = p_med_record_id
          AND status != 'CANCELLED';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_service_fee := 0;
    END;

    -- 4. Get Prescription Fee
    BEGIN
        SELECT NVL(SUM(total_price), 0)
        INTO v_prescription_fee
        FROM PRESCRIPTION
        WHERE med_record_id = p_med_record_id
          AND status != 'CANCELLED';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_prescription_fee := 0;
    END;

    -- 5. Get Admission Fee
    BEGIN
        SELECT NVL(SUM(total_price), 0)
        INTO v_admission_fee
        FROM ADMISSION_REQUEST
        WHERE med_record_id = p_med_record_id
          AND status != 'CANCELLED';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_admission_fee := 0;
    END;

    -- Calculate total
    v_total_fee := v_appt_fee + v_lab_fee + v_service_fee + v_prescription_fee + v_admission_fee;
    
    RETURN v_total_fee;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END;
/
