-- ============================================================
-- Align request workflow with sample/result lifecycle
-- ============================================================

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE LAB_TEST_REQUEST DROP CONSTRAINT chk_labreq_status';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2443 THEN
            RAISE;
        END IF;
END;
/

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE MEDICAL_SERVICE_REQUEST DROP CONSTRAINT chk_serreq_status';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2443 THEN
            RAISE;
        END IF;
END;
/

UPDATE LAB_TEST_REQUEST
SET status = CASE status
    WHEN 'PENDING' THEN 'NOT_COLLECTED'
    WHEN 'IN_PROGRESS' THEN 'SAMPLE_COLLECTED'
    WHEN 'COMPLETED' THEN 'RESULT_AVAILABLE'
    WHEN 'CANCELLED' THEN 'RESULT_AVAILABLE'
    ELSE status
END,
    total_price = CASE
        WHEN status = 'CANCELLED' THEN 0
        ELSE total_price
    END;

UPDATE MEDICAL_SERVICE_REQUEST
SET status = CASE status
    WHEN 'PENDING' THEN 'NOT_COLLECTED'
    WHEN 'IN_PROGRESS' THEN 'SAMPLE_COLLECTED'
    WHEN 'COMPLETED' THEN 'RESULT_AVAILABLE'
    WHEN 'CANCELLED' THEN 'RESULT_AVAILABLE'
    ELSE status
END,
    total_price = CASE
        WHEN status = 'CANCELLED' THEN 0
        ELSE total_price
    END;

ALTER TABLE LAB_TEST_REQUEST ADD CONSTRAINT chk_labreq_status
    CHECK (status IN ('NOT_COLLECTED', 'SAMPLE_COLLECTED', 'RESULT_AVAILABLE'));

ALTER TABLE MEDICAL_SERVICE_REQUEST ADD CONSTRAINT chk_serreq_status
    CHECK (status IN ('NOT_COLLECTED', 'SAMPLE_COLLECTED', 'RESULT_AVAILABLE'));
