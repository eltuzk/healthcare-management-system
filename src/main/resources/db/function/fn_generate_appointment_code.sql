CREATE OR REPLACE FUNCTION fn_generate_appointment_code
RETURN VARCHAR2
IS
    v_date_str VARCHAR2(8);
    v_seq_num NUMBER;
    v_code VARCHAR2(30);
BEGIN
    -- Format: APT-YYYYMMDD-XXXX
    SELECT TO_CHAR(SYSDATE, 'YYYYMMDD') INTO v_date_str FROM DUAL;
    
    -- Find the max code for today
    SELECT NVL(MAX(TO_NUMBER(SUBSTR(appointment_code, -4))), 0) + 1
    INTO v_seq_num
    FROM APPOINTMENT
    WHERE appointment_code LIKE 'APT-' || v_date_str || '-%';
    
    -- Format to 4 digits
    v_code := 'APT-' || v_date_str || '-' || LPAD(v_seq_num, 4, '0');
    
    RETURN v_code;
EXCEPTION
    WHEN OTHERS THEN
        -- Fallback
        RETURN 'APT-' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '-' || LPAD(ROUND(DBMS_RANDOM.VALUE(1, 9999)), 4, '0');
END;
/
