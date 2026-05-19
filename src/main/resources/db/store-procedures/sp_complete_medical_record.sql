CREATE OR REPLACE PROCEDURE sp_complete_medical_record (
    p_med_record_id IN NUMBER,
    p_clinical_conclusion IN CLOB,
    p_conclusion_type IN VARCHAR2
)
IS
    v_appointment_id NUMBER;
BEGIN
    -- Update Medical Record
    UPDATE MEDICAL_RECORD
    SET status = 'COMPLETED',
        clinical_conclusion = p_clinical_conclusion,
        conclusion_type = p_conclusion_type,
        completed_at = SYSTIMESTAMP,
        version_number = version_number + 1
    WHERE med_record_id = p_med_record_id;
    
    -- Get related appointment
    SELECT appointment_id INTO v_appointment_id
    FROM MEDICAL_RECORD
    WHERE med_record_id = p_med_record_id;
    
    -- Update Appointment status to COMPLETED
    IF v_appointment_id IS NOT NULL THEN
        UPDATE APPOINTMENT
        SET status = 'COMPLETED',
            updated_at = SYSTIMESTAMP,
            version_number = version_number + 1
        WHERE appointment_id = v_appointment_id;
    END IF;
    
    COMMIT;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/
