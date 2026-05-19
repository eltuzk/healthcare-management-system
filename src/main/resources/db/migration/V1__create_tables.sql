-- ============================================================
-- HEALTHCARE MANAGEMENT SYSTEM
-- Oracle DDL Script (no index)
-- Generated: 2026-04-12
-- ============================================================

-- ============================================================
-- 1. ROLE
-- ============================================================
CREATE TABLE ROLE (
                      role_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      role_name   VARCHAR2(100) NOT NULL UNIQUE,
                      description VARCHAR2(500)
);

-- ============================================================
-- 2. PERMISSION
-- ============================================================
CREATE TABLE PERMISSION (
                            permission_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            permission_name VARCHAR2(100) NOT NULL UNIQUE,
                            detail          VARCHAR2(500)
);

-- ============================================================
-- 3. ROLE_PERMISSION
-- ============================================================
CREATE TABLE ROLE_PERMISSION (
                                 role_id       NUMBER NOT NULL,
                                 permission_id NUMBER NOT NULL,
                                 CONSTRAINT pk_role_permission PRIMARY KEY (role_id, permission_id),
                                 CONSTRAINT fk_rp_role         FOREIGN KEY (role_id)       REFERENCES ROLE(role_id),
                                 CONSTRAINT fk_rp_permission   FOREIGN KEY (permission_id) REFERENCES PERMISSION(permission_id)
);

-- ============================================================
-- 4. ACCOUNT
-- ============================================================
CREATE TABLE ACCOUNT (
                         account_id    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         email         VARCHAR2(255) NOT NULL UNIQUE,
                         password_hash     VARCHAR2(255) NOT NULL,
                         role_id       NUMBER        NOT NULL,
                         is_active     NUMBER(1)     DEFAULT 1 NOT NULL,
                         CONSTRAINT fk_account_role    FOREIGN KEY (role_id) REFERENCES ROLE(role_id),
                         CONSTRAINT chk_account_active CHECK (is_active IN (0, 1))
);

-- ============================================================
-- 5. BRANCH
-- ============================================================
CREATE TABLE BRANCH (
                        branch_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        branch_name    VARCHAR2(200) NOT NULL UNIQUE,
                        branch_address VARCHAR2(500) NOT NULL,
                        branch_hotline VARCHAR2(20)
);

-- ============================================================
-- 6. ROOM_TYPE
-- ============================================================
CREATE TABLE ROOM_TYPE (
                           room_type_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           room_type_name VARCHAR2(100) NOT NULL UNIQUE
);

-- ============================================================
-- 7. ROOM
-- ============================================================
CREATE TABLE ROOM (
                      room_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      room_type_id NUMBER       NOT NULL,
                      branch_id    NUMBER       NOT NULL,
                      room_code    VARCHAR2(50) NOT NULL UNIQUE,
                      position     VARCHAR2(200),
                      note         VARCHAR2(500),
                      CONSTRAINT fk_room_type   FOREIGN KEY (room_type_id) REFERENCES ROOM_TYPE(room_type_id),
                      CONSTRAINT fk_room_branch FOREIGN KEY (branch_id)    REFERENCES BRANCH(branch_id)
);

-- ============================================================
-- 8. BED
-- ============================================================
CREATE TABLE BED (
                     bed_id  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                     room_id NUMBER        NOT NULL,
                     price   NUMBER(15, 2) NOT NULL,
                     status  VARCHAR2(20)  DEFAULT 'AVAILABLE' NOT NULL,
                     CONSTRAINT fk_bed_room    FOREIGN KEY (room_id) REFERENCES ROOM(room_id),
                     CONSTRAINT chk_bed_status CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE')),
                     CONSTRAINT chk_bed_price  CHECK (price >= 0)
);

-- ============================================================
-- 9. DOCTOR
-- ============================================================
CREATE TABLE DOCTOR (
                        doctor_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        account_id     NUMBER        NOT NULL UNIQUE,
                        full_name      VARCHAR2(200) NOT NULL,
                        qualification  VARCHAR2(200),
                        specialization VARCHAR2(200),
                        license_num    VARCHAR2(100) NOT NULL UNIQUE,
                        identity_num   VARCHAR2(50)  UNIQUE,
                        gender         VARCHAR2(10),
                        phone          VARCHAR2(20),
                        address        VARCHAR2(500),
                        date_of_birth  DATE,
                        hire_date      DATE,
                        experience     NUMBER(3),
                        is_active      NUMBER(1) DEFAULT 1 NOT NULL,
                        CONSTRAINT fk_doctor_account FOREIGN KEY (account_id) REFERENCES ACCOUNT(account_id),
                        CONSTRAINT chk_doctor_active CHECK (is_active IN (0, 1)),
                        CONSTRAINT chk_doctor_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);

-- ============================================================
-- 10. PATIENT
-- ============================================================
CREATE TABLE PATIENT (
                         patient_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         account_id      NUMBER       UNIQUE,
                         full_name       VARCHAR2(200) NOT NULL,
                         gender          VARCHAR2(10),
                         date_of_birth   DATE,
                         phone           VARCHAR2(20),
                         address         VARCHAR2(500),
                         identity_num    VARCHAR2(50) UNIQUE,
                         medical_history CLOB,
                         allergy         VARCHAR2(1000),
                         is_active       NUMBER(1) DEFAULT 1 NOT NULL,
                         CONSTRAINT fk_patient_account FOREIGN KEY (account_id) REFERENCES ACCOUNT(account_id),
                         CONSTRAINT chk_patient_active CHECK (is_active IN (0, 1)),
                         CONSTRAINT chk_patient_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);

-- ============================================================
-- 11. PATIENT_INSURANCE
-- ============================================================
CREATE TABLE PATIENT_INSURANCE (
                                   patient_insurance_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   patient_id           NUMBER        NOT NULL,
                                   insurance_num        VARCHAR2(100) NOT NULL UNIQUE,
                                   status               VARCHAR2(20)  DEFAULT 'ACTIVE' NOT NULL,
                                   expiry_date          DATE          NOT NULL,
                                   coverage_percent     NUMBER(5, 2)  NOT NULL,
                                   CONSTRAINT fk_insurance_patient FOREIGN KEY (patient_id) REFERENCES PATIENT(patient_id),
                                   CONSTRAINT chk_insurance_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'SUSPENDED')),
                                   CONSTRAINT chk_coverage_percent CHECK (coverage_percent BETWEEN 0 AND 100)
);

-- ============================================================
-- 12. LAB_TEST (danh mục xét nghiệm)
-- ============================================================
CREATE TABLE LAB_TEST (
                          lab_test_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          lab_test_name VARCHAR2(200) NOT NULL UNIQUE,
                          price         NUMBER(15, 2) NOT NULL,
                          is_active     NUMBER(1) DEFAULT 1 NOT NULL,
                          CONSTRAINT chk_labtest_price  CHECK (price >= 0),
                          CONSTRAINT chk_labtest_active CHECK (is_active IN (0, 1))
);

-- ============================================================
-- 13. MEDICAL_SERVICE (danh mục dịch vụ chức năng)
-- ============================================================
CREATE TABLE MEDICAL_SERVICE (
                                 med_service_id       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 medical_service_name VARCHAR2(200) NOT NULL UNIQUE,
                                 price                NUMBER(15, 2) NOT NULL,
                                 is_active            NUMBER(1) DEFAULT 1 NOT NULL,
                                 CONSTRAINT chk_medservice_price  CHECK (price >= 0),
                                 CONSTRAINT chk_medservice_active CHECK (is_active IN (0, 1))
);

-- ============================================================
-- 14. MEDICINE (danh mục thuốc)
-- ============================================================
CREATE TABLE MEDICINE (
                          medicine_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          medicine_name VARCHAR2(200) NOT NULL UNIQUE,
                          price         NUMBER(15, 2) NOT NULL,
                          is_active     NUMBER(1) DEFAULT 1 NOT NULL,
                          CONSTRAINT chk_medicine_price  CHECK (price >= 0),
                          CONSTRAINT chk_medicine_active CHECK (is_active IN (0, 1))
);

-- ============================================================
-- 15. MEDICINE_LOT (lô thuốc / kho)
-- ============================================================
CREATE TABLE MEDICINE_LOT (
                              lot_id             NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              medicine_id        NUMBER        NOT NULL,
                              lot_number         VARCHAR2(100) NOT NULL,
                              quantity           NUMBER(10)    NOT NULL,
                              supplier           VARCHAR2(200),
                              manufacturing_date DATE,
                              expiry_date        DATE          NOT NULL,
                              CONSTRAINT fk_lot_medicine  FOREIGN KEY (medicine_id) REFERENCES MEDICINE(medicine_id),
                              CONSTRAINT chk_lot_quantity CHECK (quantity >= 0),
                              CONSTRAINT chk_lot_expiry   CHECK (expiry_date > manufacturing_date)
);

-- ============================================================
-- 16. DOCTOR_SCHEDULE
-- ============================================================
CREATE TABLE DOCTOR_SCHEDULE (
                                 doctor_schedule_id    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 doctor_id             NUMBER      NOT NULL,
                                 room_id               NUMBER      NOT NULL,
                                 schedule_date         DATE        NOT NULL,
                                 start_time            VARCHAR2(5) NOT NULL,
                                 end_time              VARCHAR2(5) NOT NULL,
                                 max_capacity          NUMBER(5)   NOT NULL,
                                 current_booking_count NUMBER(5)   DEFAULT 0 NOT NULL,
                                 last_queue_number     NUMBER(5)   DEFAULT 0 NOT NULL,
                                 note                  VARCHAR2(500),
                                 CONSTRAINT fk_schedule_doctor    FOREIGN KEY (doctor_id) REFERENCES DOCTOR(doctor_id),
                                 CONSTRAINT fk_schedule_room      FOREIGN KEY (room_id)   REFERENCES ROOM(room_id),
                                 CONSTRAINT chk_schedule_capacity CHECK (max_capacity > 0),
                                 CONSTRAINT chk_schedule_booking  CHECK (current_booking_count >= 0),
                                 CONSTRAINT uq_schedule_doctor_date UNIQUE (doctor_id, schedule_date, start_time)
);

-- ============================================================
-- 17. APPOINTMENT
-- ============================================================
CREATE TABLE APPOINTMENT (
                             appointment_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             patient_id         NUMBER       NOT NULL,
                             doctor_schedule_id NUMBER       NOT NULL,
                             queue_num          NUMBER(5)    NOT NULL,
                             status             VARCHAR2(20) DEFAULT 'PENDING' NOT NULL,
                             created_at         TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
                             CONSTRAINT fk_appt_patient  FOREIGN KEY (patient_id)         REFERENCES PATIENT(patient_id),
                             CONSTRAINT fk_appt_schedule FOREIGN KEY (doctor_schedule_id) REFERENCES DOCTOR_SCHEDULE(doctor_schedule_id),
                             CONSTRAINT chk_appt_status  CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
                             CONSTRAINT uq_appt_patient_schedule UNIQUE (patient_id, doctor_schedule_id)
);

-- ============================================================
-- 18. MEDICAL_RECORD
-- ============================================================
CREATE TABLE MEDICAL_RECORD (
                                med_record_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                appointment_id    NUMBER        NOT NULL UNIQUE,
                                record_date       DATE          NOT NULL,
                                initial_diagnosis VARCHAR2(1000),
                                conclusion        CLOB,
                                total_price       NUMBER(15, 2) DEFAULT 0,
                                CONSTRAINT fk_medrecord_appointment FOREIGN KEY (appointment_id) REFERENCES APPOINTMENT(appointment_id),
                                CONSTRAINT chk_medrecord_price      CHECK (total_price >= 0)
);

-- ============================================================
-- 19. ADMISSION_REQUEST (nội trú)
-- ============================================================
CREATE TABLE ADMISSION_REQUEST (
                                   admission_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   patient_id     NUMBER        NOT NULL,
                                   med_record_id  NUMBER        NOT NULL UNIQUE,
                                   bed_id         NUMBER        NOT NULL,
                                   admission_date DATE          NOT NULL,
                                   discharge_date DATE,
                                   status         VARCHAR2(20)  DEFAULT 'PENDING' NOT NULL,
                                   total_price    NUMBER(15, 2) DEFAULT 0,
                                   CONSTRAINT fk_admission_patient   FOREIGN KEY (patient_id)    REFERENCES PATIENT(patient_id),
                                   CONSTRAINT fk_admission_medrecord FOREIGN KEY (med_record_id) REFERENCES MEDICAL_RECORD(med_record_id),
                                   CONSTRAINT fk_admission_bed       FOREIGN KEY (bed_id)        REFERENCES BED(bed_id),
                                   CONSTRAINT chk_admission_status   CHECK (status IN ('PENDING', 'ADMITTED', 'DISCHARGED', 'CANCELLED')),
                                   CONSTRAINT chk_admission_price    CHECK (total_price >= 0),
                                   CONSTRAINT chk_discharge_after    CHECK (discharge_date IS NULL OR discharge_date >= admission_date)
);

-- ============================================================
-- 20. ADMISSION_RECORD (theo dõi sinh hiệu)
-- ============================================================
CREATE TABLE ADMISSION_RECORD (
                                  admission_record_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  admission_id        NUMBER       NOT NULL,
                                  blood_pressure      VARCHAR2(20),
                                  heart_rate          NUMBER(5),
                                  temperature         NUMBER(5, 2),
                                  record_date         DATE         NOT NULL,
                                  CONSTRAINT fk_admrecord_admission FOREIGN KEY (admission_id) REFERENCES ADMISSION_REQUEST(admission_id)
);

-- ============================================================
-- 21. LAB_TEST_REQUEST
-- ============================================================
CREATE TABLE LAB_TEST_REQUEST (
                                  lab_test_request_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  med_record_id       NUMBER        NOT NULL,
                                  request_code        VARCHAR2(100) NOT NULL UNIQUE,
                                  status              VARCHAR2(20)  DEFAULT 'PENDING' NOT NULL,
                                  payment_status      VARCHAR2(20)  DEFAULT 'UNPAID'  NOT NULL,
                                  total_price         NUMBER(15, 2) DEFAULT 0,
                                  note                VARCHAR2(500),
                                  created_at          TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
                                  updated_at          TIMESTAMP,
                                  paid_at             TIMESTAMP,
                                  CONSTRAINT fk_labreq_medrecord  FOREIGN KEY (med_record_id) REFERENCES MEDICAL_RECORD(med_record_id),
                                  CONSTRAINT chk_labreq_status    CHECK (status         IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
                                  CONSTRAINT chk_labreq_paystatus CHECK (payment_status IN ('UNPAID', 'PAID')),
                                  CONSTRAINT chk_labreq_price     CHECK (total_price >= 0)
);

-- ============================================================
-- 22. LAB_TEST_REQUEST_ITEM
-- ============================================================
CREATE TABLE LAB_TEST_REQUEST_ITEM (
                                       lab_test_request_id NUMBER        NOT NULL,
                                       lab_test_id         NUMBER        NOT NULL,
                                       snapshot_price      NUMBER(15, 2) NOT NULL,
                                       CONSTRAINT pk_lab_request_item PRIMARY KEY (lab_test_request_id, lab_test_id),
                                       CONSTRAINT fk_labitem_request  FOREIGN KEY (lab_test_request_id) REFERENCES LAB_TEST_REQUEST(lab_test_request_id),
                                       CONSTRAINT fk_labitem_labtest  FOREIGN KEY (lab_test_id)         REFERENCES LAB_TEST(lab_test_id),
                                       CONSTRAINT chk_labitem_price   CHECK (snapshot_price >= 0)
);

-- ============================================================
-- 23. LAB_TEST_RESULT
-- ============================================================
CREATE TABLE LAB_TEST_RESULT (
                                 lab_test_result_id  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 lab_test_request_id NUMBER NOT NULL UNIQUE,
                                 result_data         CLOB,
                                 result_date         DATE   NOT NULL,
                                 CONSTRAINT fk_labresult_request FOREIGN KEY (lab_test_request_id) REFERENCES LAB_TEST_REQUEST(lab_test_request_id)
);

-- ============================================================
-- 24. MEDICAL_SERVICE_REQUEST
-- ============================================================
CREATE TABLE MEDICAL_SERVICE_REQUEST (
                                         med_ser_req_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         med_record_id  NUMBER        NOT NULL,
                                         request_code   VARCHAR2(100) NOT NULL UNIQUE,
                                         status         VARCHAR2(20)  DEFAULT 'PENDING' NOT NULL,
                                         payment_status VARCHAR2(20)  DEFAULT 'UNPAID'  NOT NULL,
                                         total_price    NUMBER(15, 2) DEFAULT 0,
                                         currency       VARCHAR2(10)  DEFAULT 'VND',
                                         note           VARCHAR2(500),
                                         created_at     TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
                                         updated_at     TIMESTAMP,
                                         confirmed_at   TIMESTAMP,
                                         cancelled_at   TIMESTAMP,
                                         paid_at        TIMESTAMP,
                                         CONSTRAINT fk_serreq_medrecord  FOREIGN KEY (med_record_id) REFERENCES MEDICAL_RECORD(med_record_id),
                                         CONSTRAINT chk_serreq_status    CHECK (status         IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
                                         CONSTRAINT chk_serreq_paystatus CHECK (payment_status IN ('UNPAID', 'PAID')),
                                         CONSTRAINT chk_serreq_price     CHECK (total_price >= 0)
);

-- ============================================================
-- 25. MEDICAL_SERVICE_REQUEST_ITEM
-- ============================================================
CREATE TABLE MEDICAL_SERVICE_REQUEST_ITEM (
                                              med_ser_req_id NUMBER        NOT NULL,
                                              med_service_id NUMBER        NOT NULL,
                                              snapshot_price NUMBER(15, 2) NOT NULL,
                                              CONSTRAINT pk_serreq_item     PRIMARY KEY (med_ser_req_id, med_service_id),
                                              CONSTRAINT fk_seritem_request FOREIGN KEY (med_ser_req_id) REFERENCES MEDICAL_SERVICE_REQUEST(med_ser_req_id),
                                              CONSTRAINT fk_seritem_service FOREIGN KEY (med_service_id) REFERENCES MEDICAL_SERVICE(med_service_id),
                                              CONSTRAINT chk_seritem_price  CHECK (snapshot_price >= 0)
);

-- ============================================================
-- 26. MEDICAL_SERVICE_RESULT
-- ============================================================
CREATE TABLE MEDICAL_SERVICE_RESULT (
                                        med_service_result_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        med_ser_req_id        NUMBER NOT NULL UNIQUE,
                                        result_data           CLOB,
                                        created_at            TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                                        CONSTRAINT fk_serresult_request FOREIGN KEY (med_ser_req_id) REFERENCES MEDICAL_SERVICE_REQUEST(med_ser_req_id)
);

-- ============================================================
-- 27. PRESCRIPTION
-- ============================================================
CREATE TABLE PRESCRIPTION (
                              prescription_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              med_record_id   NUMBER        NOT NULL UNIQUE,
                              status          VARCHAR2(20)  DEFAULT 'PENDING' NOT NULL,
                              payment_status  VARCHAR2(20)  DEFAULT 'UNPAID'  NOT NULL,
                              total_price     NUMBER(15, 2) DEFAULT 0,
                              created_at      TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
                              paid_at         TIMESTAMP,
                              CONSTRAINT fk_prescription_medrecord FOREIGN KEY (med_record_id) REFERENCES MEDICAL_RECORD(med_record_id),
                              CONSTRAINT chk_prescription_status  CHECK (status         IN ('PENDING', 'DISPENSED', 'CANCELLED')),
                              CONSTRAINT chk_prescription_pay     CHECK (payment_status IN ('UNPAID', 'PAID')),
                              CONSTRAINT chk_prescription_price   CHECK (total_price >= 0)
);

-- ============================================================
-- 28. PRESCRIPTION_DETAIL
-- ============================================================
CREATE TABLE PRESCRIPTION_DETAIL (
                                     prescription_id NUMBER        NOT NULL,
                                     medicine_id     NUMBER        NOT NULL,
                                     quantity        NUMBER(10)    NOT NULL,
                                     unit            VARCHAR2(50)  NOT NULL,
                                     instruction     VARCHAR2(500),
                                     snapshot_price  NUMBER(15, 2) NOT NULL,
                                     CONSTRAINT pk_prescription_detail PRIMARY KEY (prescription_id, medicine_id),
                                     CONSTRAINT fk_presdetail_presc    FOREIGN KEY (prescription_id) REFERENCES PRESCRIPTION(prescription_id),
                                     CONSTRAINT fk_presdetail_medicine FOREIGN KEY (medicine_id)     REFERENCES MEDICINE(medicine_id),
                                     CONSTRAINT chk_presdetail_qty     CHECK (quantity > 0),
                                     CONSTRAINT chk_presdetail_price   CHECK (snapshot_price >= 0)
);

-- ============================================================
-- 29. PAYMENT_RECORD
-- ============================================================
CREATE TABLE PAYMENT_RECORD (
                                payment_record_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                med_record_id     NUMBER        NOT NULL UNIQUE,
                                request_code      VARCHAR2(100) NOT NULL UNIQUE,
                                total_price       NUMBER(15, 2) NOT NULL,
                                payment_status    VARCHAR2(20)  DEFAULT 'UNPAID' NOT NULL,
                                created_at        TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
                                updated_at        TIMESTAMP,
                                paid_at           TIMESTAMP,
                                CONSTRAINT fk_payrecord_medrecord FOREIGN KEY (med_record_id) REFERENCES MEDICAL_RECORD(med_record_id),
                                CONSTRAINT chk_payrecord_status   CHECK (payment_status IN ('UNPAID', 'PARTIAL', 'PAID')),
                                CONSTRAINT chk_payrecord_price    CHECK (total_price >= 0)
);

-- ============================================================
-- 30. PAYMENT_TRANSACTION
-- ============================================================
CREATE TABLE PAYMENT_TRANSACTION (
                                     transaction_id       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     payment_record_id    NUMBER        NOT NULL,
                                     transfer_type        VARCHAR2(50),
                                     gateway              VARCHAR2(50)  NOT NULL,
                                     account_number       VARCHAR2(100),
                                     sepay_transaction_id VARCHAR2(200),
                                     transfer_amount      NUMBER(15, 2) NOT NULL,
                                     transaction_date     TIMESTAMP     DEFAULT SYSTIMESTAMP NOT NULL,
                                     reference_code       VARCHAR2(200),
                                     content              VARCHAR2(500),
                                     description          VARCHAR2(1000),
                                     process_status       VARCHAR2(20)  DEFAULT 'PENDING' NOT NULL,
                                     raw_data             CLOB,
                                     CONSTRAINT fk_paytxn_record  FOREIGN KEY (payment_record_id) REFERENCES PAYMENT_RECORD(payment_record_id),
                                     CONSTRAINT chk_paytxn_amount CHECK (transfer_amount > 0),
                                     CONSTRAINT chk_paytxn_status CHECK (process_status IN ('PENDING', 'SUCCESS', 'FAILED'))
);

CREATE TABLE ACCOUNT_PERMISSION (
                                    account_id    NUMBER NOT NULL,
                                    permission_id NUMBER NOT NULL,
                                    CONSTRAINT pk_account_permission PRIMARY KEY (account_id, permission_id),
                                    CONSTRAINT fk_ap_account    FOREIGN KEY (account_id)    REFERENCES ACCOUNT(account_id),
                                    CONSTRAINT fk_ap_permission FOREIGN KEY (permission_id) REFERENCES PERMISSION(permission_id)
);