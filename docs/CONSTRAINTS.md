# Danh Mục Ràng Buộc Cơ Sở Dữ Liệu (Database Constraints Catalog)

Tài liệu này ghi lại toàn bộ các ràng buộc toàn vẹn được thiết lập và thực thi cứng tại **tầng cơ sở dữ liệu (Database Layer)** bằng các câu lệnh Oracle SQL DDL. 
Các quy tắc kiểm tra động ở tầng ứng dụng (Java backend) đã được tách riêng sang tài liệu [rule_service.md](rule_service.md).
Hệ thống của bạn có tổng cộng 125 ràng buộc độc bản được định nghĩa bằng tên cụ thể:

CHECK Constraints (Ràng buộc kiểm tra điều kiện): 46 ràng buộc (Ví dụ: kiểm tra tồn kho không âm, giới hạn phần trăm bảo hiểm 0−100%, các cờ Boolean 0/1, v.v.).
FOREIGN KEY Constraints (Ràng buộc khóa ngoại): 38 ràng buộc (Đảm bảo tính toàn vẹn tham chiếu giữa các thực thể như Lịch hẹn → Bệnh nhân, Hóa đơn → Lịch hẹn, v.v.).
UNIQUE Constraints (Ràng buộc duy nhất): 37 ràng buộc (Đảm bảo không trùng lặp Email tài khoản, Số định danh CCCD, Giấy phép hành nghề của Bác sĩ, Số bảo hiểm, v.v.).
PRIMARY KEY Constraints (Ràng buộc khóa chính ghép): 4 ràng buộc (Được thiết lập thủ công trên các bảng liên kết nhiều-nhiều như ROLE_PERMISSION, ACCOUNT_PERMISSION, LAB_TEST_REQUEST_ITEM, MEDICAL_SERVICE_REQUEST_ITEM - không tính các khóa chính đơn tự động tạo khi khai báo cột PRIMARY KEY của từng bảng).
---

## Xác Thực Và Phân Quyền

* **`ROLE.role_name` là duy nhất:**
  ```sql
  ALTER TABLE ROLE ADD CONSTRAINT UQ_ROLE_NAME UNIQUE (role_name);
  ```
* **`PERMISSION.permission_name` là duy nhất:**
  ```sql
  ALTER TABLE PERMISSION ADD CONSTRAINT UQ_PERMISSION_NAME UNIQUE (permission_name);
  ```
* **`ROLE_PERMISSION` có khóa chính `(role_id, permission_id)`:**
  ```sql
  ALTER TABLE ROLE_PERMISSION ADD CONSTRAINT PK_ROLE_PERMISSION PRIMARY KEY (role_id, permission_id);
  ```
* **`ACCOUNT_PERMISSION` có khóa chính `(account_id, permission_id)`:**
  ```sql
  ALTER TABLE ACCOUNT_PERMISSION ADD CONSTRAINT PK_ACCOUNT_PERMISSION PRIMARY KEY (account_id, permission_id);
  ```
* **`ACCOUNT.email` là duy nhất:**
  ```sql
  ALTER TABLE ACCOUNT ADD CONSTRAINT UQ_ACCOUNT_EMAIL UNIQUE (email);
  ```
* **`ACCOUNT.google_id` là duy nhất nếu có giá trị:**
  ```sql
  ALTER TABLE ACCOUNT ADD CONSTRAINT uq_account_google_id UNIQUE (google_id);
  ```
* **`ACCOUNT.is_active` chỉ nhận `0` hoặc `1` (Boolean flag):**
  ```sql
  ALTER TABLE ACCOUNT ADD CONSTRAINT CHK_ACCOUNT_ACTIVE CHECK (is_active IN (0, 1));
  ```

---

## Cơ Sở Vật Chất

* **`BRANCH.branch_name` là duy nhất:**
  ```sql
  ALTER TABLE BRANCH ADD CONSTRAINT UQ_BRANCH_NAME UNIQUE (branch_name);
  ```
* **`ROOM_TYPE.room_type_name` là duy nhất:**
  ```sql
  ALTER TABLE ROOM_TYPE ADD CONSTRAINT UQ_ROOM_TYPE_NAME UNIQUE (room_type_name);
  ```
* **`ROOM.room_code` là duy nhất:**
  ```sql
  ALTER TABLE ROOM ADD CONSTRAINT UQ_ROOM_CODE UNIQUE (room_code);
  ```
* **`ROOM.room_type_id` tham chiếu `ROOM_TYPE.room_type_id`:**
  ```sql
  ALTER TABLE ROOM ADD CONSTRAINT FK_ROOM_ROOM_TYPE FOREIGN KEY (room_type_id) REFERENCES ROOM_TYPE (room_type_id);
  ```
* **`ROOM.branch_id` tham chiếu `BRANCH.branch_id`:**
  ```sql
  ALTER TABLE ROOM ADD CONSTRAINT FK_ROOM_BRANCH FOREIGN KEY (branch_id) REFERENCES BRANCH (branch_id);
  ```
* **`ROOM.specialty_id` tham chiếu `SPECIALTY.specialty_id`:**
  ```sql
  ALTER TABLE ROOM ADD CONSTRAINT fk_room_specialty FOREIGN KEY (specialty_id) REFERENCES SPECIALTY(specialty_id);
  ```
* **`BED.room_id` tham chiếu `ROOM.room_id`:**
  ```sql
  ALTER TABLE BED ADD CONSTRAINT FK_BED_ROOM FOREIGN KEY (room_id) REFERENCES ROOM (room_id);
  ```
* **`BED.status` chỉ nhận `AVAILABLE`, `OCCUPIED`, hoặc `MAINTENANCE`:**
  ```sql
  ALTER TABLE BED ADD CONSTRAINT CHK_BED_STATUS CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE'));
  ```
* **`BED.price` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE BED ADD CONSTRAINT CHK_BED_PRICE CHECK (price >= 0);
  ```

---

## Chuyên Khoa Và Bác Sĩ

* **`SPECIALTY.specialty_code` là duy nhất và không được null:**
  ```sql
  ALTER TABLE SPECIALTY MODIFY (specialty_code NOT NULL);
  ALTER TABLE SPECIALTY ADD CONSTRAINT UQ_SPECIALTY_CODE UNIQUE (specialty_code);
  ```
* **`SPECIALTY.specialty_name` là duy nhất và không được null:**
  ```sql
  ALTER TABLE SPECIALTY MODIFY (specialty_name NOT NULL);
  ALTER TABLE SPECIALTY ADD CONSTRAINT UQ_SPECIALTY_NAME UNIQUE (specialty_name);
  ```
* **`SPECIALTY.is_active` chỉ nhận `0` hoặc `1`:**
  ```sql
  ALTER TABLE SPECIALTY ADD CONSTRAINT CHK_SPECIALTY_ACTIVE CHECK (is_active IN (0, 1));
  ```
* **`DOCTOR.account_id` là duy nhất và tham chiếu `ACCOUNT.account_id`:**
  ```sql
  ALTER TABLE DOCTOR ADD CONSTRAINT UQ_DOCTOR_ACCOUNT UNIQUE (account_id);
  ALTER TABLE DOCTOR ADD CONSTRAINT FK_DOCTOR_ACCOUNT FOREIGN KEY (account_id) REFERENCES ACCOUNT (account_id);
  ```
* **`DOCTOR.license_num` là duy nhất và không được null:**
  ```sql
  ALTER TABLE DOCTOR MODIFY (license_num NOT NULL);
  ALTER TABLE DOCTOR ADD CONSTRAINT UQ_DOCTOR_LICENSE UNIQUE (license_num);
  ```
* **`DOCTOR.identity_num` là duy nhất nếu có giá trị:**
  ```sql
  ALTER TABLE DOCTOR ADD CONSTRAINT UQ_DOCTOR_IDENTITY UNIQUE (identity_num);
  ```
* **`DOCTOR.gender` chỉ nhận `MALE`, `FEMALE`, hoặc `OTHER`:**
  ```sql
  ALTER TABLE DOCTOR ADD CONSTRAINT CHK_DOCTOR_GENDER CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'));
  ```
* **`DOCTOR.is_active` chỉ nhận `0` hoặc `1`:**
  ```sql
  ALTER TABLE DOCTOR ADD CONSTRAINT CHK_DOCTOR_ACTIVE CHECK (is_active IN (0, 1));
  ```
* **`DOCTOR.specialty_id` tham chiếu `SPECIALTY.specialty_id`:**
  ```sql
  ALTER TABLE DOCTOR ADD CONSTRAINT FK_DOCTOR_SPECIALTY FOREIGN KEY (specialty_id) REFERENCES SPECIALTY (specialty_id);
  ```

---

## Bệnh Nhân

* **`PATIENT.account_id` là duy nhất nếu có giá trị và tham chiếu `ACCOUNT.account_id`:**
  ```sql
  ALTER TABLE PATIENT ADD CONSTRAINT UQ_PATIENT_ACCOUNT UNIQUE (account_id);
  ALTER TABLE PATIENT ADD CONSTRAINT FK_PATIENT_ACCOUNT FOREIGN KEY (account_id) REFERENCES ACCOUNT (account_id);
  ```
* **`PATIENT.identity_num` là duy nhất nếu có giá trị:**
  ```sql
  ALTER TABLE PATIENT ADD CONSTRAINT UQ_PATIENT_IDENTITY UNIQUE (identity_num);
  ```
* **`PATIENT.gender` chỉ nhận `MALE`, `FEMALE`, hoặc `OTHER`:**
  ```sql
  ALTER TABLE PATIENT ADD CONSTRAINT CHK_PATIENT_GENDER CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'));
  ```
* **`PATIENT.is_active` chỉ nhận `0` hoặc `1`:**
  ```sql
  ALTER TABLE PATIENT ADD CONSTRAINT CHK_PATIENT_ACTIVE CHECK (is_active IN (0, 1));
  ```

---

## Quản Trị Viên

* **`ADMINISTRATOR.account_id` là duy nhất và tham chiếu `ACCOUNT.account_id`:**
  ```sql
  ALTER TABLE ADMINISTRATOR ADD CONSTRAINT fk_admin_account FOREIGN KEY (account_id) REFERENCES ACCOUNT(account_id);
  ```
* **`ADMINISTRATOR.identity_num` là duy nhất nếu có giá trị:**
  ```sql
  -- Thiết lập qua cột UNIQUE trong định nghĩa bảng
  identity_num VARCHAR2(50) UNIQUE
  ```
* **`ADMINISTRATOR.gender` chỉ nhận `MALE`, `FEMALE`, hoặc `OTHER`:**
  ```sql
  ALTER TABLE ADMINISTRATOR ADD CONSTRAINT chk_admin_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'));
  ```
* **`ADMINISTRATOR.is_active` chỉ nhận `0` hoặc `1`:**
  ```sql
  ALTER TABLE ADMINISTRATOR ADD CONSTRAINT chk_admin_active CHECK (is_active IN (0, 1));
  ```

---

## Bảo Hiểm Bệnh Nhân

* **`PATIENT_INSURANCE.patient_id` tham chiếu `PATIENT.patient_id`:**
  ```sql
  ALTER TABLE PATIENT_INSURANCE ADD CONSTRAINT FK_INSURANCE_PATIENT FOREIGN KEY (patient_id) REFERENCES PATIENT (patient_id);
  ```
* **`PATIENT_INSURANCE.insurance_num` là duy nhất và không được null:**
  ```sql
  ALTER TABLE PATIENT_INSURANCE MODIFY (insurance_num NOT NULL);
  ALTER TABLE PATIENT_INSURANCE ADD CONSTRAINT UQ_INSURANCE_NUM UNIQUE (insurance_num);
  ```
* **`PATIENT_INSURANCE.status` chỉ nhận `ACTIVE`, `EXPIRED`, hoặc `SUSPENDED`:**
  ```sql
  ALTER TABLE PATIENT_INSURANCE ADD CONSTRAINT CHK_INSURANCE_STATUS CHECK (status IN ('ACTIVE', 'EXPIRED', 'SUSPENDED'));
  ```
* **`PATIENT_INSURANCE.coverage_percent` phải nằm trong khoảng `0` đến `100`:**
  ```sql
  ALTER TABLE PATIENT_INSURANCE ADD CONSTRAINT CHK_INSURANCE_COVERAGE CHECK (coverage_percent BETWEEN 0 AND 100);
  ```

---

## Danh Mục Dịch Vụ Và Thuốc

* **`LAB_TEST.lab_test_name` là duy nhất:**
  ```sql
  ALTER TABLE LAB_TEST ADD CONSTRAINT UQ_LAB_TEST_NAME UNIQUE (lab_test_name);
  ```
* **`LAB_TEST.price` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE LAB_TEST ADD CONSTRAINT CHK_LAB_TEST_PRICE CHECK (price >= 0);
  ```
* **`LAB_TEST.is_active` chỉ nhận `0` hoặc `1`:**
  ```sql
  ALTER TABLE LAB_TEST ADD CONSTRAINT CHK_LAB_TEST_ACTIVE CHECK (is_active IN (0, 1));
  ```
* **`MEDICAL_SERVICE.medical_service_name` là duy nhất:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE ADD CONSTRAINT UQ_MEDICAL_SERVICE_NAME UNIQUE (medical_service_name);
  ```
* **`MEDICAL_SERVICE.price` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE ADD CONSTRAINT CHK_MEDICAL_SERVICE_PRICE CHECK (price >= 0);
  ```
* **`MEDICAL_SERVICE.is_active` chỉ nhận `0` hoặc `1`:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE ADD CONSTRAINT CHK_MEDICAL_SERVICE_ACTIVE CHECK (is_active IN (0, 1));
  ```
* **`MEDICINE.medicine_name` là duy nhất:**
  ```sql
  ALTER TABLE medicine ADD CONSTRAINT uk_medicine_name UNIQUE (medicine_name);
  ```
* **`MEDICINE.is_active` chỉ nhận `0` hoặc `1`:**
  ```sql
  -- Thiết lập qua CHECK hoặc DEFAULT NOT NULL ở cột is_active
  is_active NUMBER(1) DEFAULT 1 NOT NULL
  ```
* **`MEDICINE_LOT.medicine_id` tham chiếu `MEDICINE.medicine_id`:**
  ```sql
  CONSTRAINT fk_medicine_lot_medicine FOREIGN KEY (medicine_id) REFERENCES medicine (medicine_id)
  ```
* **`(medicine_id, lot_number)` của `MEDICINE_LOT` là duy nhất (không trùng số lô của cùng một loại thuốc):**
  ```sql
  CONSTRAINT uk_medicine_lot_medicine_lot_number UNIQUE (medicine_id, lot_number)
  ```
* **`MEDICINE_LOT.quantity` phải lớn hơn hoặc bằng `0`:**
  ```sql
  CONSTRAINT chk_medicine_lot_quantity CHECK (quantity >= 0)
  ```
* **`MEDICINE_LOT.import_price` phải lớn hơn hoặc bằng `0` nếu có giá trị:**
  ```sql
  CONSTRAINT chk_medicine_lot_import_price CHECK (import_price IS NULL OR import_price >= 0)
  ```
* **`MEDICINE_LOT.expiry_date` phải sau `manufacturing_date`:**
  ```sql
  CONSTRAINT chk_medicine_lot_expiry_date CHECK (manufacturing_date IS NULL OR expiry_date > manufacturing_date)
  ```

---

## Phí Khám

* **`CONSULTATION_FEE.fee_code` là duy nhất và không được null:**
  ```sql
  ALTER TABLE CONSULTATION_FEE MODIFY (fee_code NOT NULL);
  ALTER TABLE CONSULTATION_FEE ADD CONSTRAINT UQ_CONSULTATION_FEE_CODE UNIQUE (fee_code);
  ```
* **`CONSULTATION_FEE.price` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE CONSULTATION_FEE ADD CONSTRAINT CHK_CONSULTATION_FEE_PRICE CHECK (price >= 0);
  ```
* **`CONSULTATION_FEE.is_active` chỉ nhận `0` hoặc `1`:**
  ```sql
  ALTER TABLE CONSULTATION_FEE ADD CONSTRAINT CHK_CONSULTATION_FEE_ACTIVE CHECK (is_active IN (0, 1));
  ```
* **`CONSULTATION_FEE.specialty_id` tham chiếu `SPECIALTY.specialty_id`:**
  ```sql
  ALTER TABLE CONSULTATION_FEE ADD CONSTRAINT FK_FEE_SPECIALTY FOREIGN KEY (specialty_id) REFERENCES SPECIALTY (specialty_id);
  ```
* **`CONSULTATION_FEE.specialty_id` là duy nhất nếu có giá trị:**
  ```sql
  ALTER TABLE CONSULTATION_FEE ADD CONSTRAINT UQ_FEE_SPECIALTY UNIQUE (specialty_id);
  ```

---

## Lịch Làm Việc Bác Sĩ

* **`DOCTOR_SCHEDULE.doctor_id` tham chiếu `DOCTOR.doctor_id`:**
  ```sql
  ALTER TABLE DOCTOR_SCHEDULE ADD CONSTRAINT FK_SCHEDULE_DOCTOR FOREIGN KEY (doctor_id) REFERENCES DOCTOR (doctor_id);
  ```
* **`DOCTOR_SCHEDULE.room_id` tham chiếu `ROOM.room_id`:**
  ```sql
  ALTER TABLE DOCTOR_SCHEDULE ADD CONSTRAINT FK_SCHEDULE_ROOM FOREIGN KEY (room_id) REFERENCES ROOM (room_id);
  ```
* **`DOCTOR_SCHEDULE.max_capacity` phải lớn hơn `0`:**
  ```sql
  ALTER TABLE DOCTOR_SCHEDULE ADD CONSTRAINT CHK_SCHEDULE_MAX_CAP CHECK (max_capacity > 0);
  ```
* **`DOCTOR_SCHEDULE.current_booking_count` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE DOCTOR_SCHEDULE ADD CONSTRAINT CHK_SCHEDULE_BOOKING_COUNT CHECK (current_booking_count >= 0);
  ```
* **`DOCTOR_SCHEDULE.last_queue_number` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE DOCTOR_SCHEDULE ADD CONSTRAINT CHK_SCHEDULE_LAST_QUEUE CHECK (last_queue_number >= 0);
  ```
* **`DOCTOR_SCHEDULE.shift` chỉ nhận `MORNING` hoặc `AFTERNOON`:**
  ```sql
  ALTER TABLE DOCTOR_SCHEDULE ADD CONSTRAINT CHK_SCHEDULE_SHIFT CHECK (shift IN ('MORNING', 'AFTERNOON'));
  ```
* **`DOCTOR_SCHEDULE` unique theo `(doctor_id, schedule_date, shift)`:**
  ```sql
  ALTER TABLE DOCTOR_SCHEDULE ADD CONSTRAINT UQ_SCHEDULE_DOC_DATE_SHIFT UNIQUE (doctor_id, schedule_date, shift);
  ```
* **`DOCTOR_SCHEDULE` unique theo `(room_id, schedule_date, shift)`:**
  ```sql
  ALTER TABLE DOCTOR_SCHEDULE ADD CONSTRAINT UQ_SCHEDULE_ROOM_DATE_SHIFT UNIQUE (room_id, schedule_date, shift);
  ```

---

## Appointment (Lịch Hẹn)

* **`APPOINTMENT.patient_id` tham chiếu `PATIENT.patient_id`:**
  ```sql
  ALTER TABLE APPOINTMENT ADD CONSTRAINT FK_APT_PATIENT FOREIGN KEY (patient_id) REFERENCES PATIENT (patient_id);
  ```
* **`APPOINTMENT.doctor_schedule_id` tham chiếu `DOCTOR_SCHEDULE.doctor_schedule_id`:**
  ```sql
  ALTER TABLE APPOINTMENT ADD CONSTRAINT FK_APT_SCHEDULE FOREIGN KEY (doctor_schedule_id) REFERENCES DOCTOR_SCHEDULE (doctor_schedule_id);
  ```
* **`APPOINTMENT.fee_id` tham chiếu `CONSULTATION_FEE.fee_id`:**
  ```sql
  ALTER TABLE APPOINTMENT ADD CONSTRAINT FK_APT_FEE FOREIGN KEY (fee_id) REFERENCES CONSULTATION_FEE (fee_id);
  ```
* **`APPOINTMENT.appointment_code` là duy nhất và không được null:**
  ```sql
  ALTER TABLE APPOINTMENT MODIFY (appointment_code NOT NULL);
  ALTER TABLE APPOINTMENT ADD CONSTRAINT UQ_APT_CODE UNIQUE (appointment_code);
  ```
* **`APPOINTMENT.sepay_transaction_id` là duy nhất nếu có giá trị:**
  ```sql
  ALTER TABLE APPOINTMENT ADD CONSTRAINT UQ_APT_SEPAY_TX UNIQUE (sepay_transaction_id);
  ```
* **`APPOINTMENT` unique theo `(doctor_schedule_id, queue_num)`:**
  ```sql
  ALTER TABLE APPOINTMENT ADD CONSTRAINT UQ_APT_SCHEDULE_QUEUE UNIQUE (doctor_schedule_id, queue_num);
  ```
* **`APPOINTMENT.status` chỉ nhận `PENDING`, `CONFIRMED`, `CHECKED_IN`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, hoặc `PAYMENT_EXPIRED`:**
  ```sql
  ALTER TABLE APPOINTMENT ADD CONSTRAINT CHK_APT_STATUS CHECK (status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'PAYMENT_EXPIRED'));
  ```

---

## Medical Record (Hồ Sơ Bệnh Án)

* **`MEDICAL_RECORD.appointment_id` là duy nhất và tham chiếu `APPOINTMENT.appointment_id`:**
  ```sql
  ALTER TABLE MEDICAL_RECORD ADD CONSTRAINT UQ_MR_APT UNIQUE (appointment_id);
  ALTER TABLE MEDICAL_RECORD ADD CONSTRAINT FK_MR_APT FOREIGN KEY (appointment_id) REFERENCES APPOINTMENT (appointment_id);
  ```
* **`MEDICAL_RECORD.doctor_id` tham chiếu `DOCTOR.doctor_id`:**
  ```sql
  ALTER TABLE MEDICAL_RECORD ADD CONSTRAINT FK_MR_DOCTOR FOREIGN KEY (doctor_id) REFERENCES DOCTOR (doctor_id);
  ```
* **`MEDICAL_RECORD.patient_id` tham chiếu `PATIENT.patient_id`:**
  ```sql
  ALTER TABLE MEDICAL_RECORD ADD CONSTRAINT FK_MR_PATIENT FOREIGN KEY (patient_id) REFERENCES PATIENT (patient_id);
  ```
* **`MEDICAL_RECORD.initial_diagnosis` không được null:**
  ```sql
  ALTER TABLE MEDICAL_RECORD MODIFY (initial_diagnosis NOT NULL);
  ```
* **`MEDICAL_RECORD.status` chỉ nhận `DRAFT`, `IN_PROGRESS`, `COMPLETED`, hoặc `LOCKED`:**
  ```sql
  ALTER TABLE MEDICAL_RECORD ADD CONSTRAINT CHK_MR_STATUS CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'COMPLETED', 'LOCKED'));
  ```
* **`MEDICAL_RECORD.conclusion_type` chỉ nhận `COMPLETED` hoặc `ADMISSION_REQUIRED` khi có giá trị:**
  ```sql
  ALTER TABLE MEDICAL_RECORD ADD CONSTRAINT CHK_MR_CONCLUSION CHECK (conclusion_type IN ('COMPLETED', 'ADMISSION_REQUIRED'));
  ```

---

## Nhập Viện (Hospital Admission)

* **`ADMISSION_REQUEST.patient_id` tham chiếu `PATIENT.patient_id`:**
  ```sql
  ALTER TABLE ADMISSION_REQUEST ADD CONSTRAINT FK_ADMISSION_PATIENT FOREIGN KEY (patient_id) REFERENCES PATIENT (patient_id);
  ```
* **`ADMISSION_REQUEST.med_record_id` là duy nhất và tham chiếu `MEDICAL_RECORD.med_record_id`:**
  ```sql
  ALTER TABLE ADMISSION_REQUEST ADD CONSTRAINT UQ_ADMISSION_MR UNIQUE (med_record_id);
  ALTER TABLE ADMISSION_REQUEST ADD CONSTRAINT FK_ADMISSION_MR FOREIGN KEY (med_record_id) REFERENCES MEDICAL_RECORD (med_record_id);
  ```
* **`ADMISSION_REQUEST.bed_id` tham chiếu `BED.bed_id`:**
  ```sql
  ALTER TABLE ADMISSION_REQUEST ADD CONSTRAINT FK_ADMISSION_BED FOREIGN KEY (bed_id) REFERENCES BED (bed_id);
  ```
* **`ADMISSION_REQUEST.status` chỉ nhận `PENDING`, `ADMITTED`, `DISCHARGED`, hoặc `CANCELLED`:**
  ```sql
  ALTER TABLE ADMISSION_REQUEST ADD CONSTRAINT CHK_ADMISSION_STATUS CHECK (status IN ('PENDING', 'ADMITTED', 'DISCHARGED', 'CANCELLED'));
  ```
* **`ADMISSION_REQUEST.total_price` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE ADMISSION_REQUEST ADD CONSTRAINT CHK_ADMISSION_PRICE CHECK (total_price >= 0);
  ```
* **`ADMISSION_REQUEST.discharge_date` phải null hoặc lớn hơn/bằng `admission_date`:**
  ```sql
  ALTER TABLE ADMISSION_REQUEST ADD CONSTRAINT CHK_ADMISSION_DATES CHECK (discharge_date IS NULL OR discharge_date >= admission_date);
  ```
* **`ADMISSION_RECORD.admission_id` tham chiếu `ADMISSION_REQUEST.admission_id`:**
  ```sql
  ALTER TABLE ADMISSION_RECORD ADD CONSTRAINT FK_RECORD_ADMISSION FOREIGN KEY (admission_id) REFERENCES ADMISSION_REQUEST (admission_id);
  ```

---

## Phiếu Xét Nghiệm

* **`LAB_TEST_REQUEST.med_record_id` tham chiếu `MEDICAL_RECORD.med_record_id`:**
  ```sql
  ALTER TABLE LAB_TEST_REQUEST ADD CONSTRAINT FK_LAB_REQ_MR FOREIGN KEY (med_record_id) REFERENCES MEDICAL_RECORD (med_record_id);
  ```
* **`LAB_TEST_REQUEST.request_code` là duy nhất và không được null:**
  ```sql
  ALTER TABLE LAB_TEST_REQUEST MODIFY (request_code NOT NULL);
  ALTER TABLE LAB_TEST_REQUEST ADD CONSTRAINT UQ_LAB_REQ_CODE UNIQUE (request_code);
  ```
* **`LAB_TEST_REQUEST.status` chỉ nhận `NOT_COLLECTED`, `SAMPLE_COLLECTED`, hoặc `RESULT_AVAILABLE`:**
  ```sql
  ALTER TABLE LAB_TEST_REQUEST ADD CONSTRAINT CHK_LAB_REQ_STATUS CHECK (status IN ('NOT_COLLECTED', 'SAMPLE_COLLECTED', 'RESULT_AVAILABLE'));
  ```
* **`LAB_TEST_REQUEST.payment_status` chỉ nhận `UNPAID` hoặc `PAID`:**
  ```sql
  ALTER TABLE LAB_TEST_REQUEST ADD CONSTRAINT CHK_LAB_REQ_PAY_STATUS CHECK (payment_status IN ('UNPAID', 'PAID'));
  ```
* **`LAB_TEST_REQUEST.total_price` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE LAB_TEST_REQUEST ADD CONSTRAINT CHK_LAB_REQ_PRICE CHECK (total_price >= 0);
  ```
* **`LAB_TEST_REQUEST_ITEM` có khóa chính `(lab_test_request_id, lab_test_id)`:**
  ```sql
  ALTER TABLE LAB_TEST_REQUEST_ITEM ADD CONSTRAINT PK_LAB_REQ_ITEM PRIMARY KEY (lab_test_request_id, lab_test_id);
  ```
* **`LAB_TEST_REQUEST_ITEM.lab_test_request_id` tham chiếu `LAB_TEST_REQUEST.lab_test_request_id`:**
  ```sql
  ALTER TABLE LAB_TEST_REQUEST_ITEM ADD CONSTRAINT FK_LTRI_REQ FOREIGN KEY (lab_test_request_id) REFERENCES LAB_TEST_REQUEST (lab_test_request_id);
  ```
* **`LAB_TEST_REQUEST_ITEM.lab_test_id` tham chiếu `LAB_TEST.lab_test_id`:**
  ```sql
  ALTER TABLE LAB_TEST_REQUEST_ITEM ADD CONSTRAINT FK_LTRI_TEST FOREIGN KEY (lab_test_id) REFERENCES LAB_TEST (lab_test_id);
  ```
* **`LAB_TEST_REQUEST_ITEM.snapshot_price` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE LAB_TEST_REQUEST_ITEM ADD CONSTRAINT CHK_LTRI_PRICE CHECK (snapshot_price >= 0);
  ```
* **`LAB_TEST_RESULT.lab_test_request_id` là duy nhất và tham chiếu `LAB_TEST_REQUEST.lab_test_request_id`:**
  ```sql
  ALTER TABLE LAB_TEST_RESULT ADD CONSTRAINT UQ_LAB_RESULT_REQ UNIQUE (lab_test_request_id);
  ALTER TABLE LAB_TEST_RESULT ADD CONSTRAINT FK_LAB_RESULT_REQ FOREIGN KEY (lab_test_request_id) REFERENCES LAB_TEST_REQUEST (lab_test_request_id);
  ```

---

## Phiếu Dịch Vụ Cận Lâm Sàng

* **`MEDICAL_SERVICE_REQUEST.med_record_id` tham chiếu `MEDICAL_RECORD.med_record_id`:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE_REQUEST ADD CONSTRAINT FK_SER_REQ_MR FOREIGN KEY (med_record_id) REFERENCES MEDICAL_RECORD (med_record_id);
  ```
* **`MEDICAL_SERVICE_REQUEST.request_code` là duy nhất và không được null:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE_REQUEST MODIFY (request_code NOT NULL);
  ALTER TABLE MEDICAL_SERVICE_REQUEST ADD CONSTRAINT UQ_SER_REQ_CODE UNIQUE (request_code);
  ```
* **`MEDICAL_SERVICE_REQUEST.status` chỉ nhận `NOT_COLLECTED`, `SAMPLE_COLLECTED`, hoặc `RESULT_AVAILABLE`:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE_REQUEST ADD CONSTRAINT CHK_SER_REQ_STATUS CHECK (status IN ('NOT_COLLECTED', 'SAMPLE_COLLECTED', 'RESULT_AVAILABLE'));
  ```
* **`MEDICAL_SERVICE_REQUEST.payment_status` chỉ nhận `UNPAID` hoặc `PAID`:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE_REQUEST ADD CONSTRAINT CHK_SER_REQ_PAY_STATUS CHECK (payment_status IN ('UNPAID', 'PAID'));
  ```
* **`MEDICAL_SERVICE_REQUEST.total_price` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE_REQUEST ADD CONSTRAINT CHK_SER_REQ_PRICE CHECK (total_price >= 0);
  ```
* **`MEDICAL_SERVICE_REQUEST_ITEM` có khóa chính `(med_ser_req_id, med_service_id)`:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE_REQUEST_ITEM ADD CONSTRAINT PK_SER_REQ_ITEM PRIMARY KEY (med_ser_req_id, med_service_id);
  ```
* **`MEDICAL_SERVICE_REQUEST_ITEM.med_ser_req_id` tham chiếu `MEDICAL_SERVICE_REQUEST.med_ser_req_id`:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE_REQUEST_ITEM ADD CONSTRAINT FK_MSRI_REQ FOREIGN KEY (med_ser_req_id) REFERENCES MEDICAL_SERVICE_REQUEST (med_ser_req_id);
  ```
* **`MEDICAL_SERVICE_REQUEST_ITEM.med_service_id` tham chiếu `MEDICAL_SERVICE.medical_service_id`:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE_REQUEST_ITEM ADD CONSTRAINT FK_MSRI_SER FOREIGN KEY (med_service_id) REFERENCES MEDICAL_SERVICE (medical_service_id);
  ```
* **`MEDICAL_SERVICE_REQUEST_ITEM.snapshot_price` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE_REQUEST_ITEM ADD CONSTRAINT CHK_MSRI_PRICE CHECK (snapshot_price >= 0);
  ```
* **`MEDICAL_SERVICE_RESULT.med_ser_req_id` là duy nhất và tham chiếu `MEDICAL_SERVICE_REQUEST.med_ser_req_id`:**
  ```sql
  ALTER TABLE MEDICAL_SERVICE_RESULT ADD CONSTRAINT UQ_SER_RESULT_REQ UNIQUE (med_ser_req_id);
  ALTER TABLE MEDICAL_SERVICE_RESULT ADD CONSTRAINT FK_SER_RESULT_REQ FOREIGN KEY (med_ser_req_id) REFERENCES MEDICAL_SERVICE_REQUEST (med_ser_req_id);
  ```

---

## Đơn Thuốc

* **`PRESCRIPTION.med_record_id` là duy nhất và tham chiếu `MEDICAL_RECORD.med_record_id`:**
  ```sql
  CONSTRAINT fk_prescription_medical_record FOREIGN KEY (med_record_id) REFERENCES medical_record (med_record_id)
  CONSTRAINT uk_prescription_medical_record UNIQUE (med_record_id)
  ```
* **`PRESCRIPTION_DETAIL.prescription_id` tham chiếu `PRESCRIPTION.prescription_id`:**
  ```sql
  CONSTRAINT fk_prescription_detail_prescription FOREIGN KEY (prescription_id) REFERENCES prescription (prescription_id)
  ```
* **`PRESCRIPTION_DETAIL.medicine_id` tham chiếu `MEDICINE.medicine_id`:**
  ```sql
  CONSTRAINT fk_prescription_detail_medicine FOREIGN KEY (medicine_id) REFERENCES medicine (medicine_id)
  ```
* **`PRESCRIPTION_DETAIL.quantity` phải lớn hơn hoặc bằng `1`:**
  ```sql
  CONSTRAINT chk_prescription_detail_quantity CHECK (quantity >= 1)
  ```

---

## Thanh Toán

* **`PAYMENT_RECORD.med_record_id` là duy nhất nếu có giá trị và tham chiếu `MEDICAL_RECORD.med_record_id`:**
  ```sql
  ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT UQ_PAY_MR UNIQUE (med_record_id);
  ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT FK_PAY_MR FOREIGN KEY (med_record_id) REFERENCES MEDICAL_RECORD (med_record_id);
  ```
* **`PAYMENT_RECORD.appointment_id` là duy nhất nếu có giá trị và tham chiếu `APPOINTMENT.appointment_id`:**
  ```sql
  ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT UQ_PAY_APT UNIQUE (appointment_id);
  ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT FK_PAY_APT FOREIGN KEY (appointment_id) REFERENCES APPOINTMENT (appointment_id);
  ```
* **`PAYMENT_RECORD.request_code` là duy nhất và không được null:**
  ```sql
  ALTER TABLE PAYMENT_RECORD MODIFY (request_code NOT NULL);
  ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT UQ_PAY_REQ_CODE UNIQUE (request_code);
  ```
* **`PAYMENT_RECORD.total_price` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT CHK_PAY_TOTAL_PRICE CHECK (total_price >= 0);
  ```
* **`PAYMENT_RECORD.received_amount` phải lớn hơn hoặc bằng `0`:**
  ```sql
  ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT CHK_PAY_RECEIVED_AMOUNT CHECK (received_amount >= 0);
  ```
* **`PAYMENT_RECORD.payment_status` chỉ nhận `UNPAID`, `PARTIAL`, hoặc `PAID`:**
  ```sql
  ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT CHK_PAY_STATUS CHECK (payment_status IN ('UNPAID', 'PARTIAL', 'PAID'));
  ```
* **`PAYMENT_RECORD` có owner check (chính xác một trong hai cột `med_record_id` hoặc `appointment_id` phải có giá trị):**
  ```sql
  ALTER TABLE PAYMENT_RECORD ADD CONSTRAINT CHK_PAY_OWNER CHECK (
    (med_record_id IS NOT NULL AND appointment_id IS NULL) OR
    (med_record_id IS NULL AND appointment_id IS NOT NULL)
  );
  ```
* **`PAYMENT_TRANSACTION.payment_record_id` tham chiếu `PAYMENT_RECORD.payment_record_id`:**
  ```sql
  ALTER TABLE PAYMENT_TRANSACTION ADD CONSTRAINT FK_TX_PAY_RECORD FOREIGN KEY (payment_record_id) REFERENCES PAYMENT_RECORD (payment_record_id);
  ```
* **`PAYMENT_TRANSACTION.sepay_transaction_id` là duy nhất nếu có giá trị:**
  ```sql
  ALTER TABLE PAYMENT_TRANSACTION ADD CONSTRAINT UQ_TX_SEPAY_ID UNIQUE (sepay_transaction_id);
  ```
* **`PAYMENT_TRANSACTION.confirmed_by_account_id` tham chiếu `ACCOUNT.account_id`:**
  ```sql
  ALTER TABLE PAYMENT_TRANSACTION ADD CONSTRAINT FK_TX_CONFIRMED_BY FOREIGN KEY (confirmed_by_account_id) REFERENCES ACCOUNT (account_id);
  ```
* **`PAYMENT_TRANSACTION.transfer_amount` phải lớn hơn `0`:**
  ```sql
  ALTER TABLE PAYMENT_TRANSACTION ADD CONSTRAINT CHK_TX_AMOUNT CHECK (transfer_amount > 0);
  ```
* **`PAYMENT_TRANSACTION.process_status` chỉ nhận `PENDING`, `SUCCESS`, hoặc `FAILED`:**
  ```sql
  ALTER TABLE PAYMENT_TRANSACTION ADD CONSTRAINT CHK_TX_STATUS CHECK (process_status IN ('PENDING', 'SUCCESS', 'FAILED'));
  ```
