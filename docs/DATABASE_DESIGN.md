# Database Design — Healthcare Management System

## Mục lục

1. [Mô tả từng bảng](#1-mô-tả-từng-bảng)
2. [Relationships](#2-relationships)

---

## 1. Mô tả từng bảng

> **Quy ước:**
> - `PK` = Primary Key | `FK` = Foreign Key | `UQ` = Unique | `NN` = Not Null
> - Tất cả bảng dùng `NUMBER GENERATED ALWAYS AS IDENTITY` làm PK (Oracle)
> - `is_active` mặc định `1` — dùng cho soft delete thay vì xóa thật

---

### 1.1 `ROLE` — Vai trò hệ thống

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `role_id` | NUMBER | PK | ID vai trò |
| `role_name` | VARCHAR2(100) | UQ, NN | Tên vai trò (VD: `ADMIN`, `DOCTOR`) |
| `description` | VARCHAR2(500) | | Mô tả vai trò |

> Các role mặc định: `ADMIN`, `DOCTOR`, `PATIENT`, `RECEPTIONIST`, `TECHNICIAN`, `PHARMACIST`, `ACCOUNTANT`

---

### 1.2 `PERMISSION` — Quyền hạn

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `permission_id` | NUMBER | PK | ID quyền |
| `permission_name` | VARCHAR2(100) | UQ, NN | Tên quyền |
| `detail` | VARCHAR2(500) | | Mô tả chi tiết |

---

### 1.3 `ROLE_PERMISSION` — Gán quyền cho vai trò

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `role_id` | NUMBER | PK, FK → ROLE | ID vai trò |
| `permission_id` | NUMBER | PK, FK → PERMISSION | ID quyền |

> Composite PK `(role_id, permission_id)` — dùng `@EmbeddedId` hoặc `@IdClass` trong JPA.

---

### 1.4 `ACCOUNT` — Tài khoản đăng nhập

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `account_id` | NUMBER | PK | ID tài khoản |
| `email` | VARCHAR2(255) | UQ, NN | Email đăng nhập |
| `password_hash` | VARCHAR2(255) | NN | Mật khẩu (BCrypt) |
| `role_id` | NUMBER | FK → ROLE, NN | Vai trò |
| `is_active` | NUMBER(1) | NN, DEFAULT 1 | `1` = active, `0` = đã vô hiệu hóa |

---

### 1.5 `ACCOUNT_PERMISSION` — Quyền bổ sung cho tài khoản

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `account_id` | NUMBER | PK, FK → ACCOUNT | ID tài khoản |
| `permission_id` | NUMBER | PK, FK → PERMISSION | ID quyền |

> Composite PK `(account_id, permission_id)`.
> Dùng khi cần gán thêm quyền đặc biệt cho một tài khoản cụ thể ngoài quyền đã có từ Role. Ví dụ: trưởng nhóm bác sĩ được gán thêm quyền thêm/xóa nhân viên mà không cần đổi Role.
>
> **Quyền thực tế của account = quyền từ ROLE_PERMISSION + quyền từ ACCOUNT_PERMISSION.**

---

### 1.6 `BRANCH` — Chi nhánh

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `branch_id` | NUMBER | PK | ID chi nhánh |
| `branch_name` | VARCHAR2(200) | UQ, NN | Tên chi nhánh |
| `branch_address` | VARCHAR2(500) | NN | Địa chỉ |
| `branch_hotline` | VARCHAR2(20) | | Số điện thoại |

---

### 1.6 `ROOM_TYPE` — Loại phòng

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `room_type_id` | NUMBER | PK | ID loại phòng |
| `room_type_name` | VARCHAR2(100) | UQ, NN | Tên loại phòng (VD: Phòng khám, Phòng xét nghiệm) |

---

### 1.7 `ROOM` — Phòng

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `room_id` | NUMBER | PK | ID phòng |
| `room_type_id` | NUMBER | FK → ROOM_TYPE, NN | Loại phòng |
| `branch_id` | NUMBER | FK → BRANCH, NN | Thuộc chi nhánh |
| `room_code` | VARCHAR2(50) | UQ, NN | Mã phòng |
| `position` | VARCHAR2(200) | | Vị trí phòng |
| `note` | VARCHAR2(500) | | Ghi chú |

---

### 1.8 `BED` — Giường bệnh

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `bed_id` | NUMBER | PK | ID giường |
| `room_id` | NUMBER | FK → ROOM, NN | Thuộc phòng |
| `price` | NUMBER(15,2) | NN, >= 0 | Giá/ngày |
| `status` | VARCHAR2(20) | NN, DEFAULT 'AVAILABLE' | `AVAILABLE` \| `OCCUPIED` \| `MAINTENANCE` |

> Trạng thái được quản lý tự động bởi `ADMISSION_REQUEST`: OCCUPIED khi nhập viện, AVAILABLE khi xuất viện.

---

### 1.9 `DOCTOR` — Hồ sơ bác sĩ

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `doctor_id` | NUMBER | PK | ID bác sĩ |
| `account_id` | NUMBER | FK → ACCOUNT, UQ, NN | Liên kết tài khoản (1-1) |
| `full_name` | VARCHAR2(200) | NN | Họ tên |
| `qualification` | VARCHAR2(200) | | Bằng cấp |
| `specialization` | VARCHAR2(200) | | Chuyên khoa |
| `license_num` | VARCHAR2(100) | UQ, NN | Số chứng chỉ hành nghề |
| `identity_num` | VARCHAR2(50) | UQ | Số CCCD |
| `gender` | VARCHAR2(10) | | `MALE` \| `FEMALE` \| `OTHER` |
| `phone` | VARCHAR2(20) | | Số điện thoại |
| `address` | VARCHAR2(500) | | Địa chỉ |
| `date_of_birth` | DATE | | Ngày sinh |
| `hire_date` | DATE | | Ngày vào làm |
| `experience` | NUMBER(3) | | Số năm kinh nghiệm |
| `is_active` | NUMBER(1) | NN, DEFAULT 1 | Soft delete |

---

### 1.10 `PATIENT` — Hồ sơ bệnh nhân

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `patient_id` | NUMBER | PK | ID bệnh nhân |
| `account_id` | NUMBER | FK → ACCOUNT, UQ | Liên kết tài khoản (nullable — lễ tân có thể tạo không cần account) |
| `full_name` | VARCHAR2(200) | NN | Họ tên |
| `gender` | VARCHAR2(10) | | `MALE` \| `FEMALE` \| `OTHER` |
| `date_of_birth` | DATE | | Ngày sinh |
| `phone` | VARCHAR2(20) | | Số điện thoại |
| `address` | VARCHAR2(500) | | Địa chỉ |
| `identity_num` | VARCHAR2(50) | UQ | Số CCCD |
| `medical_history` | CLOB | | Tiền sử bệnh |
| `allergy` | VARCHAR2(1000) | | Dị ứng |
| `is_active` | NUMBER(1) | NN, DEFAULT 1 | Soft delete |

---

### 1.11 `PATIENT_INSURANCE` — Bảo hiểm y tế

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `patient_insurance_id` | NUMBER | PK | ID bảo hiểm |
| `patient_id` | NUMBER | FK → PATIENT, NN | Bệnh nhân |
| `insurance_num` | VARCHAR2(100) | UQ, NN | Số thẻ bảo hiểm |
| `status` | VARCHAR2(20) | NN, DEFAULT 'ACTIVE' | `ACTIVE` \| `EXPIRED` \| `SUSPENDED` |
| `expiry_date` | DATE | NN | Ngày hết hạn |
| `coverage_percent` | NUMBER(5,2) | NN | Tỷ lệ chi trả (0–100) |

> Mỗi bệnh nhân chỉ có 1 bảo hiểm `ACTIVE` tại một thời điểm. Dùng `coverage_percent` để tính giảm giá khi thanh toán.

---

### 1.12 `LAB_TEST` — Danh mục xét nghiệm

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `lab_test_id` | NUMBER | PK | ID xét nghiệm |
| `lab_test_name` | VARCHAR2(200) | UQ, NN | Tên xét nghiệm |
| `price` | NUMBER(15,2) | NN, >= 0 | Giá hiện tại |
| `is_active` | NUMBER(1) | NN, DEFAULT 1 | Soft delete |

---

### 1.13 `MEDICAL_SERVICE` — Danh mục dịch vụ chức năng

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `med_service_id` | NUMBER | PK | ID dịch vụ |
| `medical_service_name` | VARCHAR2(200) | UQ, NN | Tên dịch vụ |
| `price` | NUMBER(15,2) | NN, >= 0 | Giá hiện tại |
| `is_active` | NUMBER(1) | NN, DEFAULT 1 | Soft delete |

---

### 1.14 `MEDICINE` — Danh mục thuốc

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `medicine_id` | NUMBER | PK | ID thuốc |
| `medicine_name` | VARCHAR2(200) | UQ, NN | Tên thuốc |
| `price` | NUMBER(15,2) | NN, >= 0 | Giá hiện tại |
| `is_active` | NUMBER(1) | NN, DEFAULT 1 | Soft delete |

---

### 1.15 `MEDICINE_LOT` — Lô thuốc / Kho

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `lot_id` | NUMBER | PK | ID lô thuốc |
| `medicine_id` | NUMBER | FK → MEDICINE, NN | Loại thuốc |
| `lot_number` | VARCHAR2(100) | NN | Số lô |
| `quantity` | NUMBER(10) | NN, >= 0 | Số lượng tồn kho |
| `supplier` | VARCHAR2(200) | | Nhà cung cấp |
| `manufacturing_date` | DATE | | Ngày sản xuất |
| `expiry_date` | DATE | NN | Ngày hết hạn |

> Khi dược sĩ cấp phát đơn thuốc: trừ tồn kho theo **FIFO** (`manufacturing_date` cũ nhất trước).

---

### 1.16 `DOCTOR_SCHEDULE` — Lịch làm việc bác sĩ

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `doctor_schedule_id` | NUMBER | PK | ID lịch |
| `doctor_id` | NUMBER | FK → DOCTOR, NN | Bác sĩ |
| `room_id` | NUMBER | FK → ROOM, NN | Phòng khám |
| `schedule_date` | DATE | NN | Ngày làm việc |
| `start_time` | VARCHAR2(5) | NN | Giờ bắt đầu (HH:MI) |
| `end_time` | VARCHAR2(5) | NN | Giờ kết thúc (HH:MI) |
| `max_capacity` | NUMBER(5) | NN, > 0 | Số bệnh nhân tối đa |
| `current_booking_count` | NUMBER(5) | NN, DEFAULT 0 | Số lịch đã đặt |
| `last_queue_number` | NUMBER(5) | NN, DEFAULT 0 | Số thứ tự cuối cùng đã cấp |
| `note` | VARCHAR2(500) | | Ghi chú |

> **Constraint:** `UNIQUE(doctor_id, schedule_date, start_time)` — Không trùng lịch.

---

### 1.17 `APPOINTMENT` — Lịch hẹn khám

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `appointment_id` | NUMBER | PK | ID lịch hẹn |
| `patient_id` | NUMBER | FK → PATIENT, NN | Bệnh nhân |
| `doctor_schedule_id` | NUMBER | FK → DOCTOR_SCHEDULE, NN | Lịch làm việc |
| `queue_num` | NUMBER(5) | NN | Số thứ tự hàng đợi |
| `status` | VARCHAR2(20) | NN, DEFAULT 'PENDING' | `PENDING` \| `CONFIRMED` \| `CANCELLED` \| `COMPLETED` |
| `created_at` | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Thời điểm đặt lịch |

> **Constraint:** `UNIQUE(patient_id, doctor_schedule_id)` — Một bệnh nhân chỉ đặt 1 lần trong 1 lịch.

---

### 1.18 `MEDICAL_RECORD` — Bệnh án

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `med_record_id` | NUMBER | PK | ID bệnh án |
| `appointment_id` | NUMBER | FK → APPOINTMENT, UQ, NN | Lịch hẹn (1-1) |
| `record_date` | DATE | NN | Ngày khám |
| `initial_diagnosis` | VARCHAR2(1000) | | Chẩn đoán ban đầu |
| `conclusion` | CLOB | | Kết luận sau khám |
| `total_price` | NUMBER(15,2) | DEFAULT 0, >= 0 | Tổng chi phí (tổng hợp từ các phiếu con) |

> Bác sĩ tạo MedicalRecord khi bắt đầu khám. Mỗi Appointment chỉ có đúng 1 MedicalRecord.

---

### 1.19 `ADMISSION_REQUEST` — Phiếu nhập viện

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `admission_id` | NUMBER | PK | ID phiếu nhập viện |
| `patient_id` | NUMBER | FK → PATIENT, NN | Bệnh nhân |
| `med_record_id` | NUMBER | FK → MEDICAL_RECORD, UQ, NN | Bệnh án chỉ định (1-1) |
| `bed_id` | NUMBER | FK → BED, NN | Giường được phân công |
| `admission_date` | DATE | NN | Ngày nhập viện |
| `discharge_date` | DATE | | Ngày xuất viện (null nếu đang nằm viện) |
| `status` | VARCHAR2(20) | NN, DEFAULT 'PENDING' | `PENDING` \| `ADMITTED` \| `DISCHARGED` \| `CANCELLED` |
| `total_price` | NUMBER(15,2) | DEFAULT 0, >= 0 | Tổng tiền giường |

> `total_price` = (DischargeDate − AdmissionDate) × BED.price, tính và cập nhật khi xuất viện.

---

### 1.20 `ADMISSION_RECORD` — Theo dõi sinh hiệu

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `admission_record_id` | NUMBER | PK | ID bản ghi |
| `admission_id` | NUMBER | FK → ADMISSION_REQUEST, NN | Đợt nằm viện |
| `blood_pressure` | VARCHAR2(20) | | Huyết áp (VD: `120/80`) |
| `heart_rate` | NUMBER(5) | | Nhịp tim (bpm) |
| `temperature` | NUMBER(5,2) | | Nhiệt độ (°C) |
| `record_date` | DATE | NN | Ngày ghi nhận |

> Được ghi nhiều lần trong một đợt nằm viện.

---

### 1.21 `LAB_TEST_REQUEST` — Phiếu chỉ định xét nghiệm

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `lab_test_request_id` | NUMBER | PK | ID phiếu |
| `med_record_id` | NUMBER | FK → MEDICAL_RECORD, NN | Bệnh án |
| `request_code` | VARCHAR2(100) | UQ, NN | Mã phiếu |
| `status` | VARCHAR2(20) | NN, DEFAULT 'PENDING' | `PENDING` \| `IN_PROGRESS` \| `COMPLETED` \| `CANCELLED` |
| `payment_status` | VARCHAR2(20) | NN, DEFAULT 'UNPAID' | `UNPAID` \| `PAID` |
| `total_price` | NUMBER(15,2) | DEFAULT 0, >= 0 | Tổng tiền (tổng snapshot_price của items) |
| `note` | VARCHAR2(500) | | Ghi chú |
| `created_at` | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | — |
| `updated_at` | TIMESTAMP | | — |
| `paid_at` | TIMESTAMP | | Thời điểm thanh toán |

---

### 1.22 `LAB_TEST_REQUEST_ITEM` — Chi tiết phiếu xét nghiệm

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `lab_test_request_id` | NUMBER | PK, FK → LAB_TEST_REQUEST | ID phiếu |
| `lab_test_id` | NUMBER | PK, FK → LAB_TEST | ID xét nghiệm |
| `snapshot_price` | NUMBER(15,2) | NN, >= 0 | Giá tại thời điểm chỉ định |

> Composite PK `(lab_test_request_id, lab_test_id)`.

---

### 1.23 `LAB_TEST_RESULT` — Kết quả xét nghiệm

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `lab_test_result_id` | NUMBER | PK | ID kết quả |
| `lab_test_request_id` | NUMBER | FK → LAB_TEST_REQUEST, UQ, NN | Phiếu xét nghiệm (1-1) |
| `result_data` | CLOB | | Nội dung kết quả |
| `result_date` | DATE | NN | Ngày có kết quả |

---

### 1.24 `MEDICAL_SERVICE_REQUEST` — Phiếu chỉ định dịch vụ chức năng

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `med_ser_req_id` | NUMBER | PK | ID phiếu |
| `med_record_id` | NUMBER | FK → MEDICAL_RECORD, NN | Bệnh án |
| `request_code` | VARCHAR2(100) | UQ, NN | Mã phiếu |
| `status` | VARCHAR2(20) | NN, DEFAULT 'PENDING' | `PENDING` \| `IN_PROGRESS` \| `COMPLETED` \| `CANCELLED` |
| `payment_status` | VARCHAR2(20) | NN, DEFAULT 'UNPAID' | `UNPAID` \| `PAID` |
| `total_price` | NUMBER(15,2) | DEFAULT 0, >= 0 | Tổng tiền |
| `currency` | VARCHAR2(10) | DEFAULT 'VND' | Đơn vị tiền tệ |
| `note` | VARCHAR2(500) | | Ghi chú |
| `created_at` | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | — |
| `updated_at` | TIMESTAMP | | — |
| `confirmed_at` | TIMESTAMP | | Thời điểm xác nhận |
| `cancelled_at` | TIMESTAMP | | Thời điểm hủy |
| `paid_at` | TIMESTAMP | | Thời điểm thanh toán |

---

### 1.25 `MEDICAL_SERVICE_REQUEST_ITEM` — Chi tiết phiếu dịch vụ

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `med_ser_req_id` | NUMBER | PK, FK → MEDICAL_SERVICE_REQUEST | ID phiếu |
| `med_service_id` | NUMBER | PK, FK → MEDICAL_SERVICE | ID dịch vụ |
| `snapshot_price` | NUMBER(15,2) | NN, >= 0 | Giá tại thời điểm chỉ định |

> Composite PK `(med_ser_req_id, med_service_id)`.

---

### 1.26 `MEDICAL_SERVICE_RESULT` — Kết quả dịch vụ chức năng

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `med_service_result_id` | NUMBER | PK | ID kết quả |
| `med_ser_req_id` | NUMBER | FK → MEDICAL_SERVICE_REQUEST, UQ, NN | Phiếu dịch vụ (1-1) |
| `result_data` | CLOB | | Nội dung kết quả |
| `created_at` | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Ngày nhập kết quả |

---

### 1.27 `PRESCRIPTION` — Đơn thuốc

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `prescription_id` | NUMBER | PK | ID đơn thuốc |
| `med_record_id` | NUMBER | FK → MEDICAL_RECORD, UQ, NN | Bệnh án (1-1) |
| `status` | VARCHAR2(20) | NN, DEFAULT 'PENDING' | `PENDING` \| `DISPENSED` \| `CANCELLED` |
| `payment_status` | VARCHAR2(20) | NN, DEFAULT 'UNPAID' | `UNPAID` \| `PAID` |
| `total_price` | NUMBER(15,2) | DEFAULT 0, >= 0 | Tổng tiền đơn thuốc |
| `created_at` | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Ngày kê đơn |
| `paid_at` | TIMESTAMP | | Thời điểm thanh toán |

---

### 1.28 `PRESCRIPTION_DETAIL` — Chi tiết đơn thuốc

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `prescription_id` | NUMBER | PK, FK → PRESCRIPTION | ID đơn thuốc |
| `medicine_id` | NUMBER | PK, FK → MEDICINE | ID thuốc |
| `quantity` | NUMBER(10) | NN, > 0 | Số lượng |
| `unit` | VARCHAR2(50) | NN | Đơn vị (viên, chai, hộp...) |
| `instruction` | VARCHAR2(500) | | Hướng dẫn sử dụng |
| `snapshot_price` | NUMBER(15,2) | NN, >= 0 | Giá tại thời điểm kê đơn |

> Composite PK `(prescription_id, medicine_id)`.
> `total_price` của Prescription = tổng `(snapshot_price × quantity)` của tất cả details.

---

### 1.29 `PAYMENT_RECORD` — Phiếu thanh toán

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `payment_record_id` | NUMBER | PK | ID phiếu |
| `med_record_id` | NUMBER | FK → MEDICAL_RECORD, UQ, NN | Bệnh án (1-1) |
| `request_code` | VARCHAR2(100) | UQ, NN | Mã phiếu thanh toán |
| `total_price` | NUMBER(15,2) | NN, >= 0 | Tổng tiền cần thanh toán |
| `payment_status` | VARCHAR2(20) | NN, DEFAULT 'UNPAID' | `UNPAID` \| `PARTIAL` \| `PAID` |
| `created_at` | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | — |
| `updated_at` | TIMESTAMP | | — |
| `paid_at` | TIMESTAMP | | Thời điểm thanh toán đủ |

> `total_price` = LAB_TEST_REQUEST(s) + MEDICAL_SERVICE_REQUEST(s) + PRESCRIPTION + ADMISSION_REQUEST (nếu có), sau đó trừ theo `PATIENT_INSURANCE.coverage_percent`.

---

### 1.30 `PAYMENT_TRANSACTION` — Giao dịch thanh toán

| Column | Type | Constraints | Mô tả |
|--------|------|-------------|-------|
| `transaction_id` | NUMBER | PK | ID giao dịch |
| `payment_record_id` | NUMBER | FK → PAYMENT_RECORD, NN | Phiếu thanh toán |
| `transfer_type` | VARCHAR2(50) | | Loại chuyển khoản |
| `gateway` | VARCHAR2(50) | NN | Cổng thanh toán (`CASH`, `BANKING`...) |
| `account_number` | VARCHAR2(100) | | Số tài khoản |
| `sepay_transaction_id` | VARCHAR2(200) | | Mã giao dịch Sepay |
| `transfer_amount` | NUMBER(15,2) | NN, > 0 | Số tiền giao dịch |
| `transaction_date` | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Thời điểm giao dịch |
| `reference_code` | VARCHAR2(200) | | Mã tham chiếu |
| `content` | VARCHAR2(500) | | Nội dung chuyển khoản |
| `description` | VARCHAR2(1000) | | Mô tả |
| `process_status` | VARCHAR2(20) | NN, DEFAULT 'PENDING' | `PENDING` \| `SUCCESS` \| `FAILED` |
| `raw_data` | CLOB | | Raw response từ cổng thanh toán |

> PaymentRecord có thể có nhiều PaymentTransaction (thanh toán nhiều lần). Khi tổng `transfer_amount` >= `total_price` → PaymentRecord chuyển sang `PAID`.

---

## 2. Relationships

### 2.1 Quan hệ 1-1 (One-to-One)

| Bảng A | Bảng B | Mô tả |
|--------|--------|-------|
| `ACCOUNT` | `DOCTOR` | Mỗi tài khoản bác sĩ có đúng 1 hồ sơ bác sĩ |
| `ACCOUNT` | `PATIENT` | Mỗi tài khoản bệnh nhân có đúng 1 hồ sơ bệnh nhân |
| `APPOINTMENT` | `MEDICAL_RECORD` | Mỗi lịch hẹn tạo ra đúng 1 bệnh án |
| `MEDICAL_RECORD` | `ADMISSION_REQUEST` | Mỗi bệnh án có tối đa 1 phiếu nhập viện |
| `MEDICAL_RECORD` | `PRESCRIPTION` | Mỗi bệnh án có tối đa 1 đơn thuốc |
| `MEDICAL_RECORD` | `PAYMENT_RECORD` | Mỗi bệnh án có đúng 1 phiếu thanh toán |
| `LAB_TEST_REQUEST` | `LAB_TEST_RESULT` | Mỗi phiếu xét nghiệm có đúng 1 kết quả |
| `MEDICAL_SERVICE_REQUEST` | `MEDICAL_SERVICE_RESULT` | Mỗi phiếu dịch vụ có đúng 1 kết quả |

### 2.2 Quan hệ 1-N (One-to-Many)

| Bảng "1" | Bảng "N" | FK | Mô tả |
|----------|----------|----|-------|
| `ROLE` | `ACCOUNT` | `account.role_id` | Một vai trò có nhiều tài khoản |
| `ROLE` | `ROLE_PERMISSION` | `role_permission.role_id` | Một vai trò có nhiều quyền |
| `PERMISSION` | `ROLE_PERMISSION` | `role_permission.permission_id` | Một quyền thuộc nhiều vai trò |
| `BRANCH` | `ROOM` | `room.branch_id` | Một chi nhánh có nhiều phòng |
| `ROOM_TYPE` | `ROOM` | `room.room_type_id` | Một loại phòng có nhiều phòng |
| `ROOM` | `BED` | `bed.room_id` | Một phòng có nhiều giường |
| `ROOM` | `DOCTOR_SCHEDULE` | `doctor_schedule.room_id` | Một phòng có nhiều lịch làm việc |
| `DOCTOR` | `DOCTOR_SCHEDULE` | `doctor_schedule.doctor_id` | Một bác sĩ có nhiều lịch làm việc |
| `DOCTOR_SCHEDULE` | `APPOINTMENT` | `appointment.doctor_schedule_id` | Một lịch làm việc có nhiều lịch hẹn |
| `PATIENT` | `APPOINTMENT` | `appointment.patient_id` | Một bệnh nhân có nhiều lịch hẹn |
| `PATIENT` | `PATIENT_INSURANCE` | `patient_insurance.patient_id` | Một bệnh nhân có nhiều bảo hiểm |
| `PATIENT` | `ADMISSION_REQUEST` | `admission_request.patient_id` | Một bệnh nhân có nhiều đợt nhập viện |
| `BED` | `ADMISSION_REQUEST` | `admission_request.bed_id` | Một giường có nhiều lần được sử dụng (theo thời gian) |
| `ADMISSION_REQUEST` | `ADMISSION_RECORD` | `admission_record.admission_id` | Một đợt nhập viện có nhiều bản ghi sinh hiệu |
| `MEDICAL_RECORD` | `LAB_TEST_REQUEST` | `lab_test_request.med_record_id` | Một bệnh án có nhiều phiếu xét nghiệm |
| `MEDICAL_RECORD` | `MEDICAL_SERVICE_REQUEST` | `medical_service_request.med_record_id` | Một bệnh án có nhiều phiếu dịch vụ |
| `LAB_TEST` | `LAB_TEST_REQUEST_ITEM` | `lab_test_request_item.lab_test_id` | Một loại xét nghiệm có trong nhiều phiếu |
| `LAB_TEST_REQUEST` | `LAB_TEST_REQUEST_ITEM` | `lab_test_request_item.lab_test_request_id` | Một phiếu chứa nhiều xét nghiệm |
| `MEDICAL_SERVICE` | `MEDICAL_SERVICE_REQUEST_ITEM` | `medical_service_request_item.med_service_id` | Một loại dịch vụ có trong nhiều phiếu |
| `MEDICAL_SERVICE_REQUEST` | `MEDICAL_SERVICE_REQUEST_ITEM` | `medical_service_request_item.med_ser_req_id` | Một phiếu chứa nhiều dịch vụ |
| `MEDICINE` | `MEDICINE_LOT` | `medicine_lot.medicine_id` | Một loại thuốc có nhiều lô |
| `MEDICINE` | `PRESCRIPTION_DETAIL` | `prescription_detail.medicine_id` | Một loại thuốc có trong nhiều đơn |
| `PRESCRIPTION` | `PRESCRIPTION_DETAIL` | `prescription_detail.prescription_id` | Một đơn thuốc có nhiều loại thuốc |
| `PAYMENT_RECORD` | `PAYMENT_TRANSACTION` | `payment_transaction.payment_record_id` | Một phiếu có nhiều giao dịch (thanh toán nhiều lần) |

### 2.3 Quan hệ N-N (Many-to-Many) — thông qua bảng junction

| Bảng A | Bảng B | Junction Table | Mô tả |
|--------|--------|----------------|-------|
| `ROLE` | `PERMISSION` | `ROLE_PERMISSION` | Một vai trò có nhiều quyền, một quyền thuộc nhiều vai trò |
| `ACCOUNT` | `PERMISSION` | `ACCOUNT_PERMISSION` | Một tài khoản có thể được gán thêm nhiều quyền bổ sung |
| `LAB_TEST_REQUEST` | `LAB_TEST` | `LAB_TEST_REQUEST_ITEM` | Một phiếu nhiều xét nghiệm, một xét nghiệm trong nhiều phiếu |
| `MEDICAL_SERVICE_REQUEST` | `MEDICAL_SERVICE` | `MEDICAL_SERVICE_REQUEST_ITEM` | Một phiếu nhiều dịch vụ, một dịch vụ trong nhiều phiếu |
| `PRESCRIPTION` | `MEDICINE` | `PRESCRIPTION_DETAIL` | Một đơn nhiều thuốc, một thuốc trong nhiều đơn |