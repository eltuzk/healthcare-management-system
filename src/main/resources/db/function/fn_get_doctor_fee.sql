CREATE OR REPLACE FUNCTION fn_get_doctor_fee (
    p_doctor_id IN NUMBER
) RETURN NUMBER
IS
    v_fee_price NUMBER(15,2);
BEGIN
    SELECT cf.price
    INTO v_fee_price
    FROM DOCTOR d
    JOIN CONSULTATION_FEE cf ON d.specialty_id = cf.specialty_id
    WHERE d.doctor_id = p_doctor_id
      AND cf.is_active = 1
      AND d.is_active = 1;
      
    RETURN v_fee_price;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN -1; -- Or raise an application error
    WHEN TOO_MANY_ROWS THEN
        -- If multiple active fees, return the first one (though business rule says 1 active fee)
        SELECT price INTO v_fee_price 
        FROM (
            SELECT cf.price FROM DOCTOR d
            JOIN CONSULTATION_FEE cf ON d.specialty_id = cf.specialty_id
            WHERE d.doctor_id = p_doctor_id AND cf.is_active = 1 AND d.is_active = 1
            ORDER BY cf.updated_at DESC
        ) WHERE ROWNUM = 1;
        RETURN v_fee_price;
    WHEN OTHERS THEN
        RETURN -1;
END;
/
