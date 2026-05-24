ALTER TABLE PAYMENT_RECORD ADD (prescription_id NUMBER(19, 0) UNIQUE);
ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT fk_payment_record_prescription FOREIGN KEY (prescription_id) REFERENCES prescription(prescription_id);
