-- ============================================================
-- Sync medical record totals and ensure each record has billing
-- ============================================================

UPDATE MEDICAL_RECORD mr
SET total_price = (
    SELECT
        COALESCE((
            SELECT SUM(ltr.total_price)
            FROM LAB_TEST_REQUEST ltr
            WHERE ltr.med_record_id = mr.med_record_id
              AND ltr.status <> 'CANCELLED'
        ), 0)
        +
        COALESCE((
            SELECT SUM(msr.total_price)
            FROM MEDICAL_SERVICE_REQUEST msr
            WHERE msr.med_record_id = mr.med_record_id
              AND msr.status <> 'CANCELLED'
        ), 0)
    FROM dual
);

UPDATE PAYMENT_RECORD pr
SET total_price = (
        SELECT COALESCE(mr.total_price, 0)
        FROM MEDICAL_RECORD mr
        WHERE mr.med_record_id = pr.med_record_id
    ),
    payment_status = CASE
        WHEN (
            SELECT COALESCE(mr.total_price, 0)
            FROM MEDICAL_RECORD mr
            WHERE mr.med_record_id = pr.med_record_id
        ) = 0 THEN 'UNPAID'
        WHEN COALESCE(pr.received_amount, 0) = 0 THEN 'UNPAID'
        WHEN COALESCE(pr.received_amount, 0) < (
            SELECT COALESCE(mr.total_price, 0)
            FROM MEDICAL_RECORD mr
            WHERE mr.med_record_id = pr.med_record_id
        ) THEN 'PARTIAL'
        ELSE 'PAID'
    END
WHERE pr.med_record_id IS NOT NULL;

INSERT INTO PAYMENT_RECORD (
    med_record_id,
    request_code,
    total_price,
    received_amount,
    payment_status,
    created_at
)
SELECT
    mr.med_record_id,
    'MR-' || TO_CHAR(mr.med_record_id),
    COALESCE(mr.total_price, 0),
    0,
    'UNPAID',
    SYSTIMESTAMP
FROM MEDICAL_RECORD mr
WHERE NOT EXISTS (
    SELECT 1
    FROM PAYMENT_RECORD pr
    WHERE pr.med_record_id = mr.med_record_id
);
