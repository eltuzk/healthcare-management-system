# Thiết Kế Database - Healthcare Management System

Tài liệu này mô tả database design hiện tại sau các migration `V1` đến `V18`.
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

### `PATIENT_INSURANCE`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `patient_insurance_id` | NUMBER | PK | ID bảo hiểm |
| `patient_id` | NUMBER | FK -> `PATIENT`, NN | Bệnh nhân |
| `insurance_num` | VARCHAR2(100) | UQ, NN | Số thẻ bảo hiểm |
| `status` | VARCHAR2(20) | NN, check | `ACTIVE`, `EXPIRED`, `SUSPENDED` |
| `expiry_date` | DATE | NN | Ngày hết hạn |
| `coverage_percent` | NUMBER(5,2) | NN, check | % bảo hiểm chi trả (0-100) |

### `ACCOUNTANT`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `accountant_id` | NUMBER | PK | ID kế toán |
| `account_id` | NUMBER | FK -> `ACCOUNT`, UQ, NN | Tài khoản đăng nhập |
| `full_name` | VARCHAR2(200) | NN | Họ tên |
| `qualification` | VARCHAR2(200) | | Bằng cấp |
| `identity_num` | VARCHAR2(50) | UQ | CCCD/CMND |
| `gender` | VARCHAR2(10) | check | `MALE`, `FEMALE`, `OTHER` |
| `phone` | VARCHAR2(20) | | Số điện thoại |
| `address` | VARCHAR2(500) | | Địa chỉ |
| `date_of_birth` | DATE | | Ngày sinh |
| `hire_date` | DATE | | Ngày vào làm |
| `experience` | NUMBER | | Số năm kinh nghiệm |
| `is_active` | NUMBER(1) | NN, default 1 | Soft delete |

### `RECEPTIONIST`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `receptionist_id` | NUMBER | PK | ID lễ tân |
| `account_id` | NUMBER | FK -> `ACCOUNT`, UQ, NN | Tài khoản đăng nhập |
| `full_name` | VARCHAR2(200) | NN | Họ tên |
| `identity_num` | VARCHAR2(50) | UQ | CCCD/CMND |
| `gender` | VARCHAR2(10) | check | `MALE`, `FEMALE`, `OTHER` |
| `phone` | VARCHAR2(20) | | Số điện thoại |
| `address` | VARCHAR2(500) | | Địa chỉ |
| `date_of_birth` | DATE | | Ngày sinh |
| `hire_date` | DATE | | Ngày vào làm |
| `shift` | VARCHAR2(50) | | Ca làm việc |
| `is_active` | NUMBER(1) | NN, default 1 | Soft delete |

### `PHARMACIST`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `pharmacist_id` | NUMBER | PK | ID dược sĩ |
| `account_id` | NUMBER | FK -> `ACCOUNT`, UQ, NN | Tài khoản đăng nhập |
| `full_name` | VARCHAR2(200) | NN | Họ tên |
| `qualification` | VARCHAR2(200) | | Bằng cấp |
| `license_num` | VARCHAR2(100) | UQ, NN | Chứng chỉ hành nghề |
| `identity_num` | VARCHAR2(50) | UQ | CCCD/CMND |
| `gender` | VARCHAR2(10) | check | `MALE`, `FEMALE`, `OTHER` |
| `phone` | VARCHAR2(20) | | Số điện thoại |
| `address` | VARCHAR2(500) | | Địa chỉ |
| `date_of_birth` | DATE | | Ngày sinh |
| `hire_date` | DATE | | Ngày vào làm |
| `experience` | NUMBER | | Số năm kinh nghiệm |
| `is_active` | NUMBER(1) | NN, default 1 | Soft delete |

### `TECHNICIAN`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `technician_id` | NUMBER | PK | ID kỹ thuật viên |
| `account_id` | NUMBER | FK -> `ACCOUNT`, UQ, NN | Tài khoản đăng nhập |
| `full_name` | VARCHAR2(200) | NN | Họ tên |
| `qualification` | VARCHAR2(200) | | Bằng cấp |
| `specialty_area` | VARCHAR2(200) | | Lĩnh vực chuyên môn (vd: X-Quang, Xét nghiệm...) |
| `license_num` | VARCHAR2(100) | UQ | Chứng chỉ hành nghề |
| `identity_num` | VARCHAR2(50) | UQ | CCCD/CMND |
| `gender` | VARCHAR2(10) | check | `MALE`, `FEMALE`, `OTHER` |
| `phone` | VARCHAR2(20) | | Số điện thoại |
| `address` | VARCHAR2(500) | | Địa chỉ |
| `date_of_birth` | DATE | | Ngày sinh |
| `hire_date` | DATE | | Ngày vào làm |
| `experience` | NUMBER | | Số năm kinh nghiệm |
| `is_active` | NUMBER(1) | NN, default 1 | Soft delete |

## Cơ Sở Vật Chất

### `BRANCH`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `branch_id` | NUMBER | PK | ID chi nhánh |
| `branch_name` | VARCHAR2(200) | UQ, NN | Tên chi nhánh |
| `branch_address` | VARCHAR2(500) | NN | Địa chỉ |
| `branch_hotline` | VARCHAR2(20) | | Số điện thoại |

### `ROOM_TYPE`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `room_type_id` | NUMBER | PK | ID loại phòng |
| `room_type_name` | VARCHAR2(100) | UQ, NN | Tên loại phòng |

### `ROOM`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `room_id` | NUMBER | PK | ID phòng |
| `room_type_id` | NUMBER | FK -> `ROOM_TYPE`, NN | Loại phòng |
| `branch_id` | NUMBER | FK -> `BRANCH`, NN | Chi nhánh |
| `room_code` | VARCHAR2(50) | UQ, NN | Mã phòng (số phòng) |
| `position` | VARCHAR2(200) | | Vị trí (tầng/khu) |
| `note` | VARCHAR2(500) | | Ghi chú |

### `BED`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `bed_id` | NUMBER | PK | ID giường |
| `room_id` | NUMBER | FK -> `ROOM`, NN | Thuộc phòng nào |
| `price` | NUMBER(15,2) | NN, >= 0 | Giá thuê giường/ngày |
| `status` | VARCHAR2(20) | NN, check | `AVAILABLE`, `OCCUPIED`, `MAINTENANCE` |

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
- Khi tạo MR, hệ thống tạo/cập nhật `PAYMENT_RECORD` owner medical record với tổng tiền ban đầu bằng `0`.
- MR `DRAFT` là giai đoạn bác sĩ ghi nhận thông tin khám ban đầu và tạo phiếu xét nghiệm/dịch vụ.
- Phiếu xét nghiệm/dịch vụ chỉ được tạo khi MR còn `DRAFT`; sau khi thanh toán và MR sang `IN_PROGRESS` thì không tạo thêm request.
- Thanh toán medical record bằng tiền mặt phải đi qua business flow payment, sync lại tổng tiền trước khi thu và chuyển MR `DRAFT -> IN_PROGRESS`.
- Request chỉ được cập nhật trạng thái/kết quả khi MR đang `IN_PROGRESS` và payment record của MR đã `PAID`.
- Khi complete MR mới bắt buộc có `initial_diagnosis`, `clinical_conclusion`, `conclusion_type`.
- MR chỉ complete khi tất cả lab test request và medical service request liên quan đã có trạng thái `RESULT_AVAILABLE`.
- Complete MR đồng thời set appointment -> `COMPLETED`.
- MR đã `LOCKED` thì không cho sửa.

## Các Bảng Danh Mục Kỹ Thuật

### `LAB_TEST`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `lab_test_id` | NUMBER | PK | ID xét nghiệm |
| `lab_test_name` | VARCHAR2(200) | UQ, NN | Tên xét nghiệm |
| `price` | NUMBER(15,2) | NN, >= 0 | Giá niêm yết |
| `is_active` | NUMBER(1) | NN, default 1 | Trạng thái |

### `MEDICAL_SERVICE`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `med_service_id` | NUMBER | PK | ID dịch vụ |
| `medical_service_name` | VARCHAR2(200) | UQ, NN | Tên dịch vụ |
| `price` | NUMBER(15,2) | NN, >= 0 | Giá niêm yết |
| `is_active` | NUMBER(1) | NN, default 1 | Trạng thái |

### `MEDICINE`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `medicine_id` | NUMBER | PK | ID thuốc |
| `medicine_name` | VARCHAR2(200) | UQ, NN | Tên thuốc |
| `price` | NUMBER(15,2) | NN, >= 0 | Giá niêm yết |
| `is_active` | NUMBER(1) | NN, default 1 | Trạng thái |

### `MEDICINE_LOT`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `lot_id` | NUMBER | PK | ID lô thuốc |
| `medicine_id` | NUMBER | FK -> `MEDICINE`, NN | Thuốc |
| `lot_number` | VARCHAR2(100) | NN | Số lô |
| `quantity` | NUMBER(10) | NN, >= 0 | Số lượng tồn |
| `supplier` | VARCHAR2(200) | | Nhà cung cấp |
| `manufacturing_date`| DATE | | Ngày sản xuất |
| `expiry_date` | DATE | NN | Ngày hết hạn |

## Quy Trình Xét Nghiệm & Dịch Vụ

### `LAB_TEST_REQUEST`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `lab_test_request_id` | NUMBER | PK | ID phiếu yêu cầu |
| `med_record_id` | NUMBER | FK -> `MEDICAL_RECORD`, NN | Hồ sơ bệnh án gốc |
| `request_code` | VARCHAR2(100) | UQ, NN | Mã phiếu |
| `status` | VARCHAR2(20) | NN, check | `NOT_COLLECTED`, `SAMPLE_COLLECTED`, `RESULT_AVAILABLE` |
| `payment_status` | VARCHAR2(20) | NN, check | `UNPAID`, `PAID` |
| `total_price` | NUMBER(15,2) | >= 0 | Tổng tiền các chỉ định |
| `note` | VARCHAR2(500) | | Ghi chú bác sĩ |
| `created_at` | TIMESTAMP | NN | Thời điểm chỉ định |
| `paid_at` | TIMESTAMP | | Thời điểm thanh toán |

- `LAB_TEST_REQUEST_ITEM`: Lưu chỉ định chi tiết, khóa chính `(lab_test_request_id, lab_test_id)`, snapshot giá tại thời điểm chỉ định.
- `LAB_TEST_RESULT`: Kết quả xét nghiệm, quan hệ 1-1 với Request, lưu dữ liệu kết quả qua cột `result_data` (CLOB).

Workflow status:

- `NOT_COLLECTED`: vừa tạo phiếu, chưa lấy mẫu/chưa thực hiện dịch vụ.
- `SAMPLE_COLLECTED`: đã lấy mẫu hoặc đã tiếp nhận thực hiện, đang chờ kết quả.
- `RESULT_AVAILABLE`: đã có bản ghi kết quả; request được xem là hoàn thành.

Quy tắc request:

- Request mặc định `NOT_COLLECTED`, `payment_status = UNPAID`, và snapshot giá từ danh mục tại thời điểm chỉ định.
- Tạo request phải lock `MEDICAL_RECORD`, validate MR đang `DRAFT`, lưu request/items, sau đó sync `MEDICAL_RECORD.total_price` và `PAYMENT_RECORD.total_price`.
- Cập nhật trạng thái thủ công chỉ cho phép chuyển sang `SAMPLE_COLLECTED`.
- Tạo `LAB_TEST_RESULT` yêu cầu request đang `SAMPLE_COLLECTED`; sau khi tạo result, hệ thống set request `RESULT_AVAILABLE`.
- Mỗi lab request chỉ có tối đa một result do ràng buộc unique trên `LAB_TEST_RESULT.lab_test_request_id`.

### `MEDICAL_SERVICE_REQUEST`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `med_ser_req_id` | NUMBER | PK | ID phiếu yêu cầu |
| `med_record_id` | NUMBER | FK -> `MEDICAL_RECORD`, NN | Hồ sơ bệnh án gốc |
| `request_code` | VARCHAR2(100) | UQ, NN | Mã phiếu |
| `status` | VARCHAR2(20) | NN, check | `NOT_COLLECTED`, `SAMPLE_COLLECTED`, `RESULT_AVAILABLE` |
| `payment_status` | VARCHAR2(20) | NN, check | `UNPAID`, `PAID` |
| `total_price` | NUMBER(15,2) | >= 0 | Tổng tiền |
| `currency` | VARCHAR2(10) | NN, default 'VND' | Loại tiền tệ |
| `created_at` | TIMESTAMP | NN | Thời điểm chỉ định |
| `paid_at` | TIMESTAMP | | Thời điểm thanh toán |

- `MEDICAL_SERVICE_REQUEST_ITEM`: Chi tiết chỉ định, khóa chính `(med_ser_req_id, med_service_id)`.
- `MEDICAL_SERVICE_RESULT`: Kết quả dịch vụ, quan hệ 1-1 với Request.

Quy tắc request:

- Request mặc định `NOT_COLLECTED`, `payment_status = UNPAID`, và snapshot giá từ danh mục tại thời điểm chỉ định.
- Tạo request phải lock `MEDICAL_RECORD`, validate MR đang `DRAFT`, lưu request/items, sau đó sync `MEDICAL_RECORD.total_price` và `PAYMENT_RECORD.total_price`.
- Cập nhật trạng thái thủ công chỉ cho phép chuyển sang `SAMPLE_COLLECTED`.
- Tạo `MEDICAL_SERVICE_RESULT` yêu cầu request đang `SAMPLE_COLLECTED`; sau khi tạo result, hệ thống set request `RESULT_AVAILABLE`.
- Mỗi service request chỉ có tối đa một result do ràng buộc unique trên `MEDICAL_SERVICE_RESULT.med_ser_req_id`.
- Sau mỗi lần tạo result, workflow service kiểm tra tất cả request của MR; nếu tất cả đều `RESULT_AVAILABLE` và MR đủ điều kiện clinical thì MR và appointment được auto-complete trong cùng transaction.

## Nhập Viện (Inpatient)

### `ADMISSION_REQUEST`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `admission_id` | NUMBER | PK | ID phiếu nhập viện |
| `patient_id` | NUMBER | FK -> `PATIENT`, NN | Bệnh nhân |
| `med_record_id` | NUMBER | FK -> `MEDICAL_RECORD`, UQ, NN | Từ bệnh án nào |
| `bed_id` | NUMBER | FK -> `BED`, NN | Giường chỉ định |
| `admission_date` | DATE | NN | Ngày nhập viện |
| `discharge_date` | DATE | | Ngày xuất viện |
| `status` | VARCHAR2(20) | NN, check | `PENDING`, `ADMITTED`, `DISCHARGED`, `CANCELLED` |
| `total_price` | NUMBER(15,2) | >= 0 | Tiền phòng/giường |

- `ADMISSION_RECORD`: Các bản ghi theo dõi (huyết áp, nhịp tim, nhiệt độ) trong quá trình nằm viện.

## Kho Thuốc Và Đơn Thuốc

### `MEDICINE`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `medicine_id` | NUMBER | PK | ID thuốc |
| `medicine_name` | VARCHAR2(200) | UQ, NN | Tên thuốc |
| `price` | NUMBER(15,2) | NN, >= 0 | Giá bán |
| `is_active` | NUMBER(1) | NN, default 1 | Trạng thái |

### `MEDICINE_LOT`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `lot_id` | NUMBER | PK | ID lô thuốc |
| `medicine_id` | NUMBER | FK -> `MEDICINE`, NN | Thuộc thuốc nào |
| `lot_number` | VARCHAR2(100) | NN | Số lô |
| `quantity` | NUMBER(10) | NN, >= 0 | Số lượng tồn kho |
| `supplier` | VARCHAR2(200) | | Nhà cung cấp |
| `manufacturing_date` | DATE | | Ngày sản xuất |
| `expiry_date` | DATE | NN | Ngày hết hạn |

### `PRESCRIPTION`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `prescription_id` | NUMBER | PK | ID đơn thuốc |
| `med_record_id` | NUMBER | FK -> `MEDICAL_RECORD`, UQ, NN | Từ bệnh án nào |
| `status` | VARCHAR2(20) | NN, check | `PENDING`, `DISPENSED`, `CANCELLED` |
| `payment_status` | VARCHAR2(20) | NN, check | `UNPAID`, `PAID` |
| `total_price` | NUMBER(15,2) | >= 0 | Tổng tiền thuốc |
| `created_at` | TIMESTAMP | NN | Ngày kê đơn |
| `paid_at` | TIMESTAMP | | Thời điểm thanh toán |

### `PRESCRIPTION_DETAIL`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `prescription_id` | NUMBER | PK, FK -> `PRESCRIPTION` | Đơn thuốc |
| `medicine_id` | NUMBER | PK, FK -> `MEDICINE` | Thuốc |
| `quantity` | NUMBER(10) | NN, > 0 | Số lượng |
| `unit` | VARCHAR2(50) | NN | Đơn vị tính |
| `snapshot_price` | NUMBER(15,2) | NN, >= 0 | Giá thuốc tại thời điểm kê đơn |
| `instruction` | VARCHAR2(500) | | Hướng dẫn sử dụng |

## Thanh Toán Và Kế Toán

### `PAYMENT_RECORD`

Đây là bảng sổ cái thanh toán phục vụ kế toán. Ứng dụng chỉ tạo/cập nhật qua business flow; API cho phép đọc sổ thanh toán và ghi nhận thanh toán tiền mặt cho medical record qua flow riêng.

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
- Khi ghi nhận thanh toán medical record, service lock MR, sync lại billing, lock payment record, validate số tiền thu khớp `total_price`, tạo `PAYMENT_TRANSACTION` gateway `CASH`, set payment `PAID`, rồi chuyển MR sang `IN_PROGRESS`.
- Báo cáo doanh thu đọc từ `PAYMENT_TRANSACTION` thành công (`process_status = SUCCESS`) và tính theo `transaction_date`.
- Revenue owner type được suy ra từ owner của `PAYMENT_RECORD`: có `appointment_id` là `APPOINTMENT`, có `med_record_id` là `MEDICAL_RECORD`.

Báo cáo doanh thu:

- API `GET /reports/revenue` chỉ dành cho `ADMIN` và `ACCOUNTANT`.
- Filter hỗ trợ `fromDate`, `toDate`, `gateway`, `ownerType`.
- Mặc định kỳ báo cáo là từ ngày đầu tháng hiện tại đến hôm nay nếu request không truyền date range.
- Kết quả gồm `totalRevenue`, `transactionCount`, breakdown theo ngày, gateway và owner type.

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

### `ROLE`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `role_id` | NUMBER | PK | ID role |
| `role_name` | VARCHAR2(100) | UQ, NN | Tên role (ADMIN, DOCTOR, ...) |
| `description` | VARCHAR2(500) | | Mô tả |

### `PERMISSION`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `permission_id` | NUMBER | PK | ID quyền |
| `permission_name` | VARCHAR2(100) | UQ, NN | Tên quyền |
| `detail` | VARCHAR2(500) | | Chi tiết |

### `ROLE_PERMISSION`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `role_id` | NUMBER | PK, FK | Link tới `ROLE` |
| `permission_id` | NUMBER | PK, FK | Link tới `PERMISSION` |

### `ACCOUNT`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `account_id` | NUMBER | PK | ID tài khoản |
| `email` | VARCHAR2(255) | UQ, NN | Email đăng nhập |
| `password_hash` | VARCHAR2(255) | NN | Password đã hash |
| `role_id` | NUMBER | FK -> `ROLE`, NN | Role chính |
| `is_active` | NUMBER(1) | NN, default 1 | Trạng thái |

### `ACCOUNT_PERMISSION`

| Cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
| --- | --- | --- | --- |
| `account_id` | NUMBER | PK, FK | Link tới `ACCOUNT` |
| `permission_id` | NUMBER | PK, FK | Link tới `PERMISSION` |

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
| `MEDICINE` | `MEDICINE_LOT` | `MEDICINE_LOT.medicine_id` |

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
- MR billing là tổng các lab test request và medical service request của MR; tổng tiền này phải sync sang payment record trước khi thu tiền.
- Request/result flow được điều khiển bằng service transaction, không dựa vào client tự cập nhật trạng thái tùy ý.
