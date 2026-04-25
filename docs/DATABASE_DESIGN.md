# Thiết Kế Database - Healthcare Management System

Tài liệu này mô tả database design hiện tại sau các migration `V1` đến `V15`.
Khi thay đổi schema hoặc business rule, cần cập nhật file này cùng với [CONSTRAINTS.md](CONSTRAINTS.md).

## Quy Ước

- `PK`: khóa chính.
- `FK`: khóa ngoại.
- `UQ`: ràng buộc duy nhất.
- `NN`: không được null.
- Các bảng chính dùng Oracle identity column cho khóa chính.
- `is_active = 1` nghĩa là đang hoạt động, `0` nghĩa là đã vô hiệu hóa/soft delete.
- Các cột `*_snapshot` lưu giá trị tại thời điểm phát sinh nghiệp vụ, không tính lại theo master data sau này.

## Tổng Quan Module

Hệ thống được chia thành các nhóm dữ liệu chính:

- Xác thực/phân quyền: `ROLE`, `PERMISSION`, `ROLE_PERMISSION`, `ACCOUNT`, `ACCOUNT_PERMISSION`.
- Cơ sở vật chất: `BRANCH`, `ROOM_TYPE`, `ROOM`, `BED`.
- Master data lâm sàng: `SPECIALTY`, `DOCTOR`, `PATIENT`, `PATIENT_INSURANCE`.
- Danh mục giá/dịch vụ: `CONSULTATION_FEE`, `LAB_TEST`, `MEDICAL_SERVICE`, `MEDICINE`, `MEDICINE_LOT`.
- Vòng đời lịch khám: `DOCTOR_SCHEDULE`, `APPOINTMENT`.
- Quy trình khám bệnh: `MEDICAL_RECORD`, các bảng request/result xét nghiệm, dịch vụ, nhập viện, đơn thuốc.
- Kế toán/thanh toán: `PAYMENT_RECORD`, `PAYMENT_TRANSACTION`.

## Master Data Chính

### `SPECIALTY`

`SPECIALTY` là bảng master chuyên khoa. Đây là nguồn dữ liệu chuẩn để map bác sĩ và giá khám.

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `specialty_id` | NUMBER | PK | ID chuyên khoa |
| `specialty_code` | VARCHAR2(50) | UQ, NN | Mã chuyên khoa |
| `specialty_name` | VARCHAR2(200) | UQ, NN | Tên chuyên khoa |
| `is_active` | NUMBER(1) | NN, default 1 | Trạng thái |
| `created_at` | TIMESTAMP | NN | Thời điểm tạo |
| `updated_at` | TIMESTAMP | | Thời điểm cập nhật |

### `DOCTOR`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `doctor_id` | NUMBER | PK | ID bác sĩ |
| `account_id` | NUMBER | FK -> `ACCOUNT`, UQ, NN | Tài khoản đăng nhập |
| `specialty_id` | NUMBER | FK -> `SPECIALTY` | Chuyên khoa chuẩn để tính giá |
| `full_name` | VARCHAR2(200) | NN | Họ tên |
| `qualification` | VARCHAR2(200) | | Bằng cấp |
| `specialization` | VARCHAR2(200) | | Text legacy/hiển thị, không dùng để tính giá |
| `license_num` | VARCHAR2(100) | UQ, NN | Chứng chỉ hành nghề |
| `identity_num` | VARCHAR2(50) | UQ | CCCD/CMND |
| `gender` | VARCHAR2(10) | check | `MALE`, `FEMALE`, `OTHER` |
| `phone` | VARCHAR2(20) | | Số điện thoại |
| `address` | VARCHAR2(500) | | Địa chỉ |
| `date_of_birth` | DATE | | Ngày sinh |
| `hire_date` | DATE | | Ngày vào làm |
| `experience` | VARCHAR2(500) | | Kinh nghiệm dạng text |
| `is_active` | NUMBER(1) | NN, default 1 | Soft delete |

### `PATIENT`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `patient_id` | NUMBER | PK | ID bệnh nhân |
| `account_id` | NUMBER | FK -> `ACCOUNT`, UQ | Nullable để lễ tân tạo hồ sơ walk-in |
| `full_name` | VARCHAR2(200) | NN | Họ tên |
| `gender` | VARCHAR2(10) | check | `MALE`, `FEMALE`, `OTHER` |
| `date_of_birth` | DATE | | Ngày sinh |
| `phone` | VARCHAR2(20) | | Số điện thoại |
| `address` | VARCHAR2(500) | | Địa chỉ |
| `identity_num` | VARCHAR2(50) | UQ | CCCD/CMND |
| `medical_history` | CLOB | | Tiền sử bệnh |
| `allergy` | VARCHAR2(1000) | | Dị ứng |
| `is_active` | NUMBER(1) | NN, default 1 | Soft delete |

## Cơ Sở Vật Chất

### `BRANCH`, `ROOM_TYPE`, `ROOM`, `BED`

- `BRANCH.branch_name` là duy nhất.
- `ROOM_TYPE.room_type_name` là duy nhất.
- `ROOM` thuộc một `BRANCH` và một `ROOM_TYPE`; `room_code` là duy nhất.
- `BED` thuộc một `ROOM`, có `price >= 0`, `status` gồm `AVAILABLE`, `OCCUPIED`, `MAINTENANCE`.

## Phí Khám

### `CONSULTATION_FEE`

`CONSULTATION_FEE` là bảng giá khám theo chuyên khoa. Appointment không so sánh text nữa, mà resolve fee qua `doctor.specialty_id`.

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `fee_id` | NUMBER | PK | ID bảng giá |
| `fee_code` | VARCHAR2(50) | UQ, NN | Mã phí |
| `fee_name` | VARCHAR2(200) | NN | Tên phí khám |
| `specialty_id` | NUMBER | FK -> `SPECIALTY`, UQ | Chuyên khoa áp dụng |
| `specialty` | VARCHAR2(100) | UQ, NN | Text legacy/hiển thị |
| `price` | NUMBER(15,2) | NN, >= 0 | Giá khám |
| `is_active` | NUMBER(1) | NN, default 1 | Trạng thái |
| `created_at` | TIMESTAMP | NN | Thời điểm tạo |
| `updated_at` | TIMESTAMP | | Thời điểm cập nhật |

Quy tắc nghiệp vụ:

- Tạo/sửa phí khám bắt buộc chọn `specialty_id` đang active.
- Mỗi chuyên khoa hiện tại chỉ nên có một fee active.
- `specialty` chỉ là text legacy/hiển thị; không dùng để tính tiền.
- Khi tạo appointment, fee được snapshot vào `APPOINTMENT.fee_name_snapshot` và `APPOINTMENT.fee_price_snapshot`.

## Lịch Làm Việc Và Appointment

### `DOCTOR_SCHEDULE`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `doctor_schedule_id` | NUMBER | PK | ID ca khám |
| `doctor_id` | NUMBER | FK -> `DOCTOR`, NN | Bác sĩ |
| `room_id` | NUMBER | FK -> `ROOM`, NN | Phòng khám |
| `schedule_date` | DATE | NN | Ngày khám |
| `shift` | VARCHAR2(20) | NN, check | `MORNING`, `AFTERNOON` |
| `max_capacity` | NUMBER(5) | NN, > 0 | Sức chứa tối đa |
| `current_booking_count` | NUMBER(5) | NN, default 0 | Số slot đang giữ/đã đặt |
| `last_queue_number` | NUMBER(5) | NN, default 0 | Số thứ tự cuối cùng đã cấp |
| `note` | VARCHAR2(500) | | Ghi chú |
| `created_at` | TIMESTAMP | NN | Thời điểm tạo |
| `updated_at` | TIMESTAMP | | Thời điểm cập nhật |
| `version_number` | NUMBER(19) | NN, default 0 | Optimistic locking |

Unique constraints:

- `(doctor_id, schedule_date, shift)`.
- `(room_id, schedule_date, shift)`.

Concurrency rule:

- Khi tạo appointment online/walk-in, service lock row schedule để cập nhật `current_booking_count` và `last_queue_number`.
- `last_queue_number` không giảm lại khi payment hết hạn; queue có thể có khoảng trống.

### `APPOINTMENT`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `appointment_id` | NUMBER | PK | ID lịch hẹn |
| `appointment_code` | VARCHAR2(30) | UQ, NN | Mã lịch hẹn, dùng cho nội dung thanh toán |
| `patient_id` | NUMBER | FK -> `PATIENT`, NN | Bệnh nhân |
| `doctor_schedule_id` | NUMBER | FK -> `DOCTOR_SCHEDULE`, NN | Ca khám |
| `fee_id` | NUMBER | FK -> `CONSULTATION_FEE` | Phí khám đã chọn |
| `fee_name_snapshot` | VARCHAR2(200) | | Tên phí tại thời điểm tạo |
| `fee_price_snapshot` | NUMBER(15,2) | | Giá phí tại thời điểm tạo |
| `queue_num` | NUMBER(5) | UQ theo schedule | Số thứ tự khám |
| `status` | VARCHAR2(20) | NN, check | Trạng thái appointment |
| `initial_symptoms` | CLOB | | Triệu chứng ban đầu |
| `visit_reason` | VARCHAR2(500) | | Lý do khám |
| `created_at` | TIMESTAMP | NN | Thời điểm tạo |
| `updated_at` | TIMESTAMP | | Thời điểm cập nhật |
| `paid_at` | TIMESTAMP | | Thời điểm đã thanh toán |
| `checked_in_at` | TIMESTAMP | | Thời điểm check-in |
| `cancelled_at` | TIMESTAMP | | Thời điểm hủy |
| `payment_expires_at` | TIMESTAMP | | Hạn thanh toán online |
| `sepay_transaction_id` | NUMBER(19) | UQ nullable | Legacy tracking trên appointment |
| `payment_reference_code` | VARCHAR2(200) | | Mã tham chiếu thanh toán |
| `payment_content` | VARCHAR2(1000) | | Nội dung thanh toán |
| `version_number` | NUMBER(19) | NN, default 0 | Optimistic locking |

Trạng thái hợp lệ:

- `PENDING`: online booking đã giữ slot, chờ thanh toán.
- `CONFIRMED`: online booking đã thanh toán đủ.
- `CHECKED_IN`: bệnh nhân đã có mặt, hoặc walk-in đã thu tiền mặt.
- `IN_PROGRESS`: bác sĩ bắt đầu khám.
- `COMPLETED`: hoàn tất khám.
- `CANCELLED`: hủy lịch chưa thanh toán.
- `PAYMENT_EXPIRED`: online booking quá hạn thanh toán, slot được release.

Flow online:

1. Bệnh nhân chọn ca khám.
2. Hệ thống lấy bác sĩ từ `DOCTOR_SCHEDULE`, lấy `specialty_id`, tìm `CONSULTATION_FEE` active.
3. Tạo appointment `PENDING`, cấp `queue_num`, snapshot phí khám, tăng `current_booking_count`, set `payment_expires_at`.
4. Tạo `PAYMENT_RECORD` owner appointment với trạng thái `UNPAID`.
5. Webhook SePay trả về thì validate secret, account, nội dung, appointment code, amount exact match.
6. Nếu hợp lệ và chưa hết hạn: appointment -> `CONFIRMED`, payment record -> `PAID`, tạo `PAYMENT_TRANSACTION` gateway `SEPAY`.
7. Nếu quá hạn: scheduler set `PAYMENT_EXPIRED` và giảm `current_booking_count`.

Flow walk-in:

1. Lễ tân chọn bệnh nhân, ca khám/phòng/bác sĩ.
2. Hệ thống resolve phí khám theo `doctor.specialty_id`.
3. Lễ tân thu đủ tiền mặt trước.
4. Tạo appointment `CHECKED_IN`, snapshot phí khám, cấp queue, set `paid_at`, `checked_in_at`.
5. Tạo `PAYMENT_RECORD` trạng thái `PAID` và `PAYMENT_TRANSACTION` gateway `CASH`.

Quy tắc hủy:

- Appointment đã thanh toán thì không được hủy.
- Walk-in tạo xong là đã paid/check-in nên không đi theo flow `CONFIRMED`.

## Medical Record

### `MEDICAL_RECORD`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `med_record_id` | NUMBER | PK | ID hồ sơ khám |
| `appointment_id` | NUMBER | FK -> `APPOINTMENT`, UQ, NN | Appointment nguồn |
| `doctor_id` | NUMBER | FK -> `DOCTOR`, NN | Bác sĩ phụ trách |
| `patient_id` | NUMBER | FK -> `PATIENT`, NN | Bệnh nhân |
| `record_date` | DATE | nullable | Legacy/date field |
| `initial_diagnosis` | VARCHAR2(1000) | | Chẩn đoán ban đầu |
| `clinical_conclusion` | CLOB | nullable khi draft | Kết luận cuối |
| `conclusion_type` | VARCHAR2(30) | nullable khi draft | `COMPLETED`, `ADMISSION_REQUIRED` |
| `clinical_notes` | CLOB | | Ghi chú lâm sàng |
| `treatment_plan` | CLOB | | Hướng điều trị |
| `status` | VARCHAR2(20) | NN, check | `DRAFT`, `IN_PROGRESS`, `COMPLETED`, `LOCKED` |
| `total_price` | NUMBER(15,2) | >= 0 | Tổng tiền phát sinh sau khám |
| `created_at` | TIMESTAMP | NN | Thời điểm tạo |
| `updated_at` | TIMESTAMP | | Thời điểm cập nhật |
| `completed_at` | TIMESTAMP | | Thời điểm hoàn tất |
| `locked_at` | TIMESTAMP | | Thời điểm khóa |
| `version_number` | NUMBER(19) | NN, default 0 | Optimistic locking |

Quy tắc nghiệp vụ:

- `createFromAppointment` tạo MR ban đầu, không bắt buộc nhập conclusion.
- MR draft có thể chưa có `clinical_conclusion` và `conclusion_type`.
- Khi complete MR mới bắt buộc có `initial_diagnosis`, `clinical_conclusion`, `conclusion_type`.
- Complete MR đồng thời set appointment -> `COMPLETED`.
- MR đã `LOCKED` thì không cho sửa.

## Các Bảng Request/Result

### Xét Nghiệm

- `LAB_TEST`: danh mục xét nghiệm, `lab_test_name` unique, `price >= 0`, `is_active`.
- `LAB_TEST_REQUEST`: thuộc `MEDICAL_RECORD`, có `request_code` unique, `status`, `payment_status`, `total_price`, `paid_at`.
- `LAB_TEST_REQUEST_ITEM`: composite PK `(lab_test_request_id, lab_test_id)`, lưu `snapshot_price`.
- `LAB_TEST_RESULT`: quan hệ 1-1 với `LAB_TEST_REQUEST`.

### Dịch Vụ Cận Lâm Sàng

- `MEDICAL_SERVICE`: danh mục dịch vụ, `medical_service_name` unique, `price >= 0`, `is_active`.
- `MEDICAL_SERVICE_REQUEST`: thuộc `MEDICAL_RECORD`, có `request_code` unique, `status`, `payment_status`, `total_price`.
- `MEDICAL_SERVICE_REQUEST_ITEM`: composite PK `(med_ser_req_id, med_service_id)`, lưu `snapshot_price`.
- `MEDICAL_SERVICE_RESULT`: quan hệ 1-1 với `MEDICAL_SERVICE_REQUEST`.

### Nhập Viện

- `ADMISSION_REQUEST`: thuộc `PATIENT`, `MEDICAL_RECORD`, `BED`.
- `ADMISSION_REQUEST.med_record_id` unique, mỗi MR tối đa một phiếu nhập viện.
- `status`: `PENDING`, `ADMITTED`, `DISCHARGED`, `CANCELLED`.
- `discharge_date` phải null hoặc >= `admission_date`.
- `ADMISSION_RECORD`: các bản ghi sinh hiệu trong đợt nằm viện.

### Đơn Thuốc

- `PRESCRIPTION`: quan hệ 1-1 với `MEDICAL_RECORD`, có `status`, `payment_status`, `total_price`.
- `PRESCRIPTION_DETAIL`: composite PK `(prescription_id, medicine_id)`, có `quantity`, `unit`, `instruction`, `snapshot_price`.
- Payment prescription để phase sau.

## Thanh Toán Và Kế Toán

### `PAYMENT_RECORD`

Đây là bảng sổ cái thanh toán phục vụ kế toán. Ứng dụng chỉ tạo/cập nhật qua business flow; API hiện tại chỉ nên đọc.

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `payment_record_id` | NUMBER | PK | ID payment record |
| `appointment_id` | NUMBER | FK -> `APPOINTMENT`, UQ nullable | Owner payment cho appointment |
| `med_record_id` | NUMBER | FK -> `MEDICAL_RECORD`, UQ nullable | Owner payment cho medical record |
| `request_code` | VARCHAR2(100) | UQ, NN | Mã phiếu thanh toán |
| `total_price` | NUMBER(15,2) | NN, >= 0 | Tổng cần thu |
| `received_amount` | NUMBER(15,2) | NN, >= 0 | Tổng đã thu |
| `payment_status` | VARCHAR2(20) | NN, check | `UNPAID`, `PARTIAL`, `PAID` |
| `created_at` | TIMESTAMP | NN | Thời điểm tạo |
| `updated_at` | TIMESTAMP | | Thời điểm cập nhật |
| `paid_at` | TIMESTAMP | | Thời điểm thu đủ |

Quy tắc owner:

- Chính xác một trong hai cột `appointment_id` hoặc `med_record_id` phải có giá trị.
- Appointment payment được ghi nhận khi đặt lịch/khám trực tiếp.
- Medical record payment sẽ gom các khoản phát sinh sau khám, như xét nghiệm/dịch vụ; prescription để phase sau.

### `PAYMENT_TRANSACTION`

`PAYMENT_TRANSACTION` là bảng chi tiết giao dịch thanh toán.

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `transaction_id` | NUMBER | PK | ID transaction |
| `payment_record_id` | NUMBER | FK -> `PAYMENT_RECORD`, NN | Payment record cha |
| `gateway` | VARCHAR2(50) | NN | `SEPAY`, `CASH`, ... |
| `transfer_type` | VARCHAR2(50) | | Loại giao dịch SePay nếu có |
| `account_number` | VARCHAR2(100) | | Tài khoản nhận/chuyển |
| `sepay_transaction_id` | VARCHAR2(200) | UQ nullable | ID giao dịch SePay |
| `transfer_amount` | NUMBER(15,2) | NN, > 0 | Số tiền |
| `transaction_date` | TIMESTAMP | NN | Thời điểm giao dịch |
| `reference_code` | VARCHAR2(200) | | Mã tham chiếu |
| `content` | VARCHAR2(500) | | Nội dung chuyển khoản |
| `description` | VARCHAR2(1000) | | Mô tả |
| `process_status` | VARCHAR2(20) | NN, check | `PENDING`, `SUCCESS`, `FAILED` |
| `raw_data` | CLOB | | Payload gateway |
| `receipt_number` | VARCHAR2(100) | | Số biên lai tiền mặt nếu có |
| `confirmed_by_account_id` | NUMBER | FK -> `ACCOUNT` | Người xác nhận/thu tiền |

Quy tắc gateway:

- Online banking: tạo transaction gateway `SEPAY`, lưu payload/mã giao dịch từ webhook.
- Walk-in cash: tạo transaction gateway `CASH`; các field gateway banking có thể null, nhưng `transfer_amount`, `process_status`, `confirmed_by_account_id` nên có.

## Bảng Xác Thực Và Phân Quyền

### `ROLE`, `PERMISSION`, `ROLE_PERMISSION`, `ACCOUNT`, `ACCOUNT_PERMISSION`

- `ROLE.role_name` unique.
- `PERMISSION.permission_name` unique.
- `ROLE_PERMISSION` PK `(role_id, permission_id)`.
- `ACCOUNT.email` unique; `ACCOUNT.role_id` FK -> `ROLE`.
- `ACCOUNT_PERMISSION` PK `(account_id, permission_id)`.
- Quyền thực tế của user = quyền theo role + quyền bổ sung theo account.

Ghi chú hiện tại:

- Trong giai đoạn dev/test, `ADMIN` đang được bypass/grant tất cả role authorities trong `JwtFilter`.

## Tóm Tắt Quan Hệ

### Quan Hệ 1-1

| A | B | Ghi chú |
| --- | --- | --- |
| `ACCOUNT` | `DOCTOR` | Một account bác sĩ có một hồ sơ doctor |
| `ACCOUNT` | `PATIENT` | Một account patient có một hồ sơ patient |
| `APPOINTMENT` | `MEDICAL_RECORD` | Một appointment tối đa một MR |
| `MEDICAL_RECORD` | `ADMISSION_REQUEST` | Một MR tối đa một admission request |
| `MEDICAL_RECORD` | `PRESCRIPTION` | Một MR tối đa một prescription |
| `APPOINTMENT` | `PAYMENT_RECORD` | Một appointment payment có một record |
| `MEDICAL_RECORD` | `PAYMENT_RECORD` | Một MR payment có một record |
| `LAB_TEST_REQUEST` | `LAB_TEST_RESULT` | Một request có một result |
| `MEDICAL_SERVICE_REQUEST` | `MEDICAL_SERVICE_RESULT` | Một request có một result |

### Quan Hệ 1-N

| Bảng 1 | Bảng N | FK |
| --- | --- | --- |
| `SPECIALTY` | `DOCTOR` | `DOCTOR.specialty_id` |
| `SPECIALTY` | `CONSULTATION_FEE` | `CONSULTATION_FEE.specialty_id` |
| `DOCTOR` | `DOCTOR_SCHEDULE` | `DOCTOR_SCHEDULE.doctor_id` |
| `ROOM` | `DOCTOR_SCHEDULE` | `DOCTOR_SCHEDULE.room_id` |
| `DOCTOR_SCHEDULE` | `APPOINTMENT` | `APPOINTMENT.doctor_schedule_id` |
| `PATIENT` | `APPOINTMENT` | `APPOINTMENT.patient_id` |
| `MEDICAL_RECORD` | `LAB_TEST_REQUEST` | `LAB_TEST_REQUEST.med_record_id` |
| `MEDICAL_RECORD` | `MEDICAL_SERVICE_REQUEST` | `MEDICAL_SERVICE_REQUEST.med_record_id` |
| `PAYMENT_RECORD` | `PAYMENT_TRANSACTION` | `PAYMENT_TRANSACTION.payment_record_id` |

### Quan Hệ N-N Qua Junction Table

| A | B | Junction |
| --- | --- | --- |
| `ROLE` | `PERMISSION` | `ROLE_PERMISSION` |
| `ACCOUNT` | `PERMISSION` | `ACCOUNT_PERMISSION` |
| `LAB_TEST_REQUEST` | `LAB_TEST` | `LAB_TEST_REQUEST_ITEM` |
| `MEDICAL_SERVICE_REQUEST` | `MEDICAL_SERVICE` | `MEDICAL_SERVICE_REQUEST_ITEM` |
| `PRESCRIPTION` | `MEDICINE` | `PRESCRIPTION_DETAIL` |

## Các Quyết Định Thiết Kế Quan Trọng

- Giá khám không được lấy bằng cách so sánh text `specialization` và `specialty`; phải đi qua `SPECIALTY.specialty_id`.
- Appointment online giữ slot ngay khi tạo `PENDING`; slot được release nếu hết hạn thanh toán.
- Queue number đã cấp không rollback để tránh duplicate/nhầm lẫn audit.
- Walk-in là trường hợp đã thu tiền mặt trước, nên appointment tạo ra ở trạng thái `CHECKED_IN`.
- Payment data phục vụ kế toán nằm ở `PAYMENT_RECORD` và `PAYMENT_TRANSACTION`; không tạo bảng payment riêng cho appointment.
- MR draft không bắt buộc có kết luận; kết luận chỉ bắt buộc khi complete.
