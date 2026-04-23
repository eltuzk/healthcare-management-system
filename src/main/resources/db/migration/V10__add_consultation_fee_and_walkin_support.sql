-- ============================================================
-- Add consultation fee catalog and walk-in payment support
-- ============================================================

CREATE TABLE CONSULTATION_FEE (
    fee_id             NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fee_code           VARCHAR2(50)  NOT NULL UNIQUE,
    fee_name           VARCHAR2(200) NOT NULL,
    specialty          VARCHAR2(100),
    price              NUMBER(15, 2) NOT NULL,
    is_active          NUMBER(1) DEFAULT 1 NOT NULL,
    created_at         TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at         TIMESTAMP,
    CONSTRAINT chk_consultation_fee_price CHECK (price >= 0),
    CONSTRAINT chk_consultation_fee_active CHECK (is_active IN (0, 1))
);

ALTER TABLE APPOINTMENT ADD (
    fee_id             NUMBER,
    fee_name_snapshot  VARCHAR2(200),
    fee_price_snapshot NUMBER(15, 2)
);

ALTER TABLE APPOINTMENT ADD CONSTRAINT fk_appt_fee
    FOREIGN KEY (fee_id) REFERENCES CONSULTATION_FEE(fee_id);

ALTER TABLE PAYMENT_TRANSACTION ADD (
    receipt_number           VARCHAR2(100),
    confirmed_by_account_id  NUMBER
);

ALTER TABLE PAYMENT_TRANSACTION ADD CONSTRAINT fk_paytxn_confirmed_by
    FOREIGN KEY (confirmed_by_account_id) REFERENCES ACCOUNT(account_id);
