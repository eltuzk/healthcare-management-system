# Danh Mục Ràng Buộc

Tài liệu này ghi lại các ràng buộc database và rule nghiệp vụ hiện tại của dự án.
Khi thêm migration Flyway hoặc đổi business flow, cần cập nhật file này cùng với [DATABASE_DESIGN.md](DATABASE_DESIGN.md).

## Xác Thực Và Phân Quyền

- `ROLE.role_name` là duy nhất.
- `PERMISSION.permission_name` là duy nhất.
- `ROLE_PERMISSION` có khóa chính `(role_id, permission_id)`.
- `ACCOUNT_PERMISSION` có khóa chính `(account_id, permission_id)`.
- `ACCOUNT.email` là duy nhất.
- `ACCOUNT.is_active` chỉ nhận `0` hoặc `1`.
- Rule tạm thời: `ADMIN` đang được cấp toàn bộ quyền role trong `JwtFilter` để phục vụ giai đoạn test/dev.

## Cơ Sở Vật Chất

- `BRANCH.branch_name` là duy nhất.
- `ROOM_TYPE.room_type_name` là duy nhất.
- `ROOM.room_code` là duy nhất.
- `ROOM.room_type_id` tham chiếu `ROOM_TYPE.room_type_id`.
- `ROOM.branch_id` tham chiếu `BRANCH.branch_id`.
- `BED.room_id` tham chiếu `ROOM.room_id`.
- `BED.status` chỉ nhận `AVAILABLE`, `OCCUPIED`, hoặc `MAINTENANCE`.
- `BED.price` phải lớn hơn hoặc bằng `0`.

## Chuyên Khoa Và Bác Sĩ

- `SPECIALTY.specialty_code` là duy nhất và không được null.
- `SPECIALTY.specialty_name` là duy nhất và không được null.
- `SPECIALTY.is_active` chỉ nhận `0` hoặc `1`.
- `DOCTOR.account_id` là duy nhất và tham chiếu `ACCOUNT.account_id`.
- `DOCTOR.license_num` là duy nhất và không được null.
- `DOCTOR.identity_num` là duy nhất nếu có giá trị.
- `DOCTOR.gender` chỉ nhận `MALE`, `FEMALE`, hoặc `OTHER`.
- `DOCTOR.is_active` chỉ nhận `0` hoặc `1`.
- `DOCTOR.specialty_id` tham chiếu `SPECIALTY.specialty_id`.
- Rule service: tạo/cập nhật bác sĩ bắt buộc chọn một chuyên khoa đang active.
- Rule service: `DOCTOR.specialization` chỉ còn là text legacy/hiển thị; tính giá khám phải dùng `DOCTOR.specialty_id`.

## Bệnh Nhân

- `PATIENT.account_id` là duy nhất nếu có giá trị và tham chiếu `ACCOUNT.account_id`.
- `PATIENT.identity_num` là duy nhất nếu có giá trị.
- `PATIENT.gender` chỉ nhận `MALE`, `FEMALE`, hoặc `OTHER`.
- `PATIENT.is_active` chỉ nhận `0` hoặc `1`.
- Rule service: tạo/cập nhật bệnh nhân phải chặn trùng `identity_num`.
- Rule service: input `gender` được chuẩn hóa về uppercase trước khi lưu.

## Bảo Hiểm Bệnh Nhân

- `PATIENT_INSURANCE.patient_id` tham chiếu `PATIENT.patient_id`.
- `PATIENT_INSURANCE.insurance_num` là duy nhất và không được null.
- `PATIENT_INSURANCE.status` chỉ nhận `ACTIVE`, `EXPIRED`, hoặc `SUSPENDED`.
- `PATIENT_INSURANCE.coverage_percent` phải nằm trong khoảng `0` đến `100`.

## Danh Mục Dịch Vụ Và Thuốc

- `LAB_TEST.lab_test_name` là duy nhất.
- `LAB_TEST.price` phải lớn hơn hoặc bằng `0`.
- `LAB_TEST.is_active` chỉ nhận `0` hoặc `1`.
- `MEDICAL_SERVICE.medical_service_name` là duy nhất.
- `MEDICAL_SERVICE.price` phải lớn hơn hoặc bằng `0`.
- `MEDICAL_SERVICE.is_active` chỉ nhận `0` hoặc `1`.
- `MEDICINE.medicine_name` là duy nhất.
- `MEDICINE.price` phải lớn hơn hoặc bằng `0`.
- `MEDICINE.is_active` chỉ nhận `0` hoặc `1`.
- `MEDICINE_LOT.medicine_id` tham chiếu `MEDICINE.medicine_id`.
- `MEDICINE_LOT.quantity` phải lớn hơn hoặc bằng `0`.
- `MEDICINE_LOT.expiry_date` phải sau `manufacturing_date`.

## Phí Khám

- `CONSULTATION_FEE.fee_code` là duy nhất và không được null.
- `CONSULTATION_FEE.price` phải lớn hơn hoặc bằng `0`.
- `CONSULTATION_FEE.is_active` chỉ nhận `0` hoặc `1`.
- `CONSULTATION_FEE.specialty` là text legacy/hiển thị và đang unique từ migration `V11`.
- `CONSULTATION_FEE.specialty_id` tham chiếu `SPECIALTY.specialty_id`.
- `CONSULTATION_FEE.specialty_id` là duy nhất nếu có giá trị.
- Rule service: tạo/cập nhật phí khám bắt buộc chọn một chuyên khoa đang active.
- Rule service: appointment phải tính giá bằng `specialty_id`, không được so sánh text `specialization` và `specialty`.
- Rule service: mỗi chuyên khoa chỉ nên có một mức phí khám hiện hành/active.

## Lịch Làm Việc Bác Sĩ

- `DOCTOR_SCHEDULE.doctor_id` tham chiếu `DOCTOR.doctor_id`.
- `DOCTOR_SCHEDULE.room_id` tham chiếu `ROOM.room_id`.
- `DOCTOR_SCHEDULE.max_capacity` phải lớn hơn `0`.
- `DOCTOR_SCHEDULE.current_booking_count` phải lớn hơn hoặc bằng `0`.
- `DOCTOR_SCHEDULE.last_queue_number` phải lớn hơn hoặc bằng `0`.
- `DOCTOR_SCHEDULE.shift` chỉ nhận `MORNING` hoặc `AFTERNOON`.
- `DOCTOR_SCHEDULE` unique theo `(doctor_id, schedule_date, shift)`.
- `DOCTOR_SCHEDULE` unique theo `(room_id, schedule_date, shift)`.
- `DOCTOR_SCHEDULE.version_number` dùng cho optimistic locking.
- Rule service: ngày tạo lịch không được nằm trong quá khứ.
- Rule service: `max_capacity` không được nhỏ hơn `current_booking_count`.
- Rule service: cập nhật queue và booking count phải lock row lịch khám.

## Appointment

- `APPOINTMENT.patient_id` tham chiếu `PATIENT.patient_id`.
- `APPOINTMENT.doctor_schedule_id` tham chiếu `DOCTOR_SCHEDULE.doctor_schedule_id`.
- `APPOINTMENT.fee_id` tham chiếu `CONSULTATION_FEE.fee_id`.
- `APPOINTMENT.appointment_code` là duy nhất và không được null.
- `APPOINTMENT.sepay_transaction_id` là duy nhất nếu có giá trị.
- `APPOINTMENT` unique theo `(doctor_schedule_id, queue_num)`.
- `APPOINTMENT.status` chỉ nhận `PENDING`, `CONFIRMED`, `CHECKED_IN`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, hoặc `PAYMENT_EXPIRED`.
- `APPOINTMENT.version_number` dùng cho optimistic locking.
- Rule service: online booking giữ slot ngay khi tạo, cấp `queue_num`, và set `payment_expires_at`.
- Rule service: online payment phải xác nhận trước `payment_expires_at`.
- Rule service: reservation online hết hạn được chuyển sang `PAYMENT_EXPIRED` và release một slot.
- Rule service: xác nhận SePay phải kiểm tra secret, hướng tiền, số tài khoản, appointment code, đúng số tiền, và trạng thái pending.
- Rule service: appointment walk-in thanh toán tiền mặt được tạo ở trạng thái `CHECKED_IN`, không phải `CONFIRMED`.
- Rule service: appointment đã thanh toán không được hủy.
- Rule service: `last_queue_number` không bao giờ giảm; cho phép có khoảng trống số thứ tự.

## Medical Record

- `MEDICAL_RECORD.appointment_id` là duy nhất và tham chiếu `APPOINTMENT.appointment_id`.
- `MEDICAL_RECORD.doctor_id` tham chiếu `DOCTOR.doctor_id`.
- `MEDICAL_RECORD.patient_id` tham chiếu `PATIENT.patient_id`.
- `MEDICAL_RECORD.initial_diagnosis` không được null.
- `MEDICAL_RECORD.conclusion_type` được phép null khi hồ sơ còn draft.
- `MEDICAL_RECORD.status` chỉ nhận `DRAFT`, `IN_PROGRESS`, `COMPLETED`, hoặc `LOCKED`.
- `MEDICAL_RECORD.conclusion_type` chỉ nhận `COMPLETED` hoặc `ADMISSION_REQUIRED` khi có giá trị.
- `MEDICAL_RECORD.version_number` dùng cho optimistic locking.
- Rule service: chỉ tạo medical record khi appointment đang `IN_PROGRESS`.
- Rule service: một appointment chỉ có tối đa một medical record.
- Rule service: tạo medical record ban đầu không yêu cầu các trường kết luận cuối.
- Rule service: complete medical record bắt buộc có `initial_diagnosis`, `clinical_conclusion`, và `conclusion_type`.
- Rule service: complete medical record đồng thời complete appointment.
- Rule service: medical record đã `LOCKED` thì không được sửa.
- Ghi chú: `MedicalRecordConclusionType.COMPLETED` là tên legacy, nghĩa là điều trị ngoại trú/kết thúc khám, không phải status của hồ sơ.

## Nhập Viện

- `ADMISSION_REQUEST.patient_id` tham chiếu `PATIENT.patient_id`.
- `ADMISSION_REQUEST.med_record_id` là duy nhất và tham chiếu `MEDICAL_RECORD.med_record_id`.
- `ADMISSION_REQUEST.bed_id` tham chiếu `BED.bed_id`.
- `ADMISSION_REQUEST.status` chỉ nhận `PENDING`, `ADMITTED`, `DISCHARGED`, hoặc `CANCELLED`.
- `ADMISSION_REQUEST.total_price` phải lớn hơn hoặc bằng `0`.
- `ADMISSION_REQUEST.discharge_date` phải null hoặc lớn hơn/bằng `admission_date`.
- `ADMISSION_RECORD.admission_id` tham chiếu `ADMISSION_REQUEST.admission_id`.
- Rule service: chỉ được tạo phiếu nhập viện khi `MedicalRecord.conclusion_type = ADMISSION_REQUIRED`.

## Phiếu Xét Nghiệm

- `LAB_TEST_REQUEST.med_record_id` tham chiếu `MEDICAL_RECORD.med_record_id`.
- `LAB_TEST_REQUEST.request_code` là duy nhất và không được null.
- `LAB_TEST_REQUEST.status` chỉ nhận `PENDING`, `IN_PROGRESS`, `COMPLETED`, hoặc `CANCELLED`.
- `LAB_TEST_REQUEST.payment_status` chỉ nhận `UNPAID` hoặc `PAID`.
- `LAB_TEST_REQUEST.total_price` phải lớn hơn hoặc bằng `0`.
- `LAB_TEST_REQUEST_ITEM` có khóa chính `(lab_test_request_id, lab_test_id)`.
- `LAB_TEST_REQUEST_ITEM.lab_test_request_id` tham chiếu `LAB_TEST_REQUEST.lab_test_request_id`.
- `LAB_TEST_REQUEST_ITEM.lab_test_id` tham chiếu `LAB_TEST.lab_test_id`.
- `LAB_TEST_REQUEST_ITEM.snapshot_price` phải lớn hơn hoặc bằng `0`.
- `LAB_TEST_RESULT.lab_test_request_id` là duy nhất và tham chiếu `LAB_TEST_REQUEST.lab_test_request_id`.

## Phiếu Dịch Vụ Cận Lâm Sàng

- `MEDICAL_SERVICE_REQUEST.med_record_id` tham chiếu `MEDICAL_RECORD.med_record_id`.
- `MEDICAL_SERVICE_REQUEST.request_code` là duy nhất và không được null.
- `MEDICAL_SERVICE_REQUEST.status` chỉ nhận `PENDING`, `IN_PROGRESS`, `COMPLETED`, hoặc `CANCELLED`.
- `MEDICAL_SERVICE_REQUEST.payment_status` chỉ nhận `UNPAID` hoặc `PAID`.
- `MEDICAL_SERVICE_REQUEST.total_price` phải lớn hơn hoặc bằng `0`.
- `MEDICAL_SERVICE_REQUEST_ITEM` có khóa chính `(med_ser_req_id, med_service_id)`.
- `MEDICAL_SERVICE_REQUEST_ITEM.med_ser_req_id` tham chiếu `MEDICAL_SERVICE_REQUEST.med_ser_req_id`.
- `MEDICAL_SERVICE_REQUEST_ITEM.med_service_id` tham chiếu `MEDICAL_SERVICE.med_service_id`.
- `MEDICAL_SERVICE_REQUEST_ITEM.snapshot_price` phải lớn hơn hoặc bằng `0`.
- `MEDICAL_SERVICE_RESULT.med_ser_req_id` là duy nhất và tham chiếu `MEDICAL_SERVICE_REQUEST.med_ser_req_id`.

## Đơn Thuốc

- `PRESCRIPTION.med_record_id` là duy nhất và tham chiếu `MEDICAL_RECORD.med_record_id`.
- `PRESCRIPTION.status` chỉ nhận `PENDING`, `DISPENSED`, hoặc `CANCELLED`.
- `PRESCRIPTION.payment_status` chỉ nhận `UNPAID` hoặc `PAID`.
- `PRESCRIPTION.total_price` phải lớn hơn hoặc bằng `0`.
- `PRESCRIPTION_DETAIL` có khóa chính `(prescription_id, medicine_id)`.
- `PRESCRIPTION_DETAIL.prescription_id` tham chiếu `PRESCRIPTION.prescription_id`.
- `PRESCRIPTION_DETAIL.medicine_id` tham chiếu `MEDICINE.medicine_id`.
- `PRESCRIPTION_DETAIL.quantity` phải lớn hơn `0`.
- `PRESCRIPTION_DETAIL.snapshot_price` phải lớn hơn hoặc bằng `0`.
- Ghi chú dự án: payment cho prescription để phase sau.

## Thanh Toán

- `PAYMENT_RECORD.med_record_id` là duy nhất nếu có giá trị và tham chiếu `MEDICAL_RECORD.med_record_id`.
- `PAYMENT_RECORD.appointment_id` là duy nhất nếu có giá trị và tham chiếu `APPOINTMENT.appointment_id`.
- `PAYMENT_RECORD.request_code` là duy nhất và không được null.
- `PAYMENT_RECORD.total_price` phải lớn hơn hoặc bằng `0`.
- `PAYMENT_RECORD.received_amount` phải lớn hơn hoặc bằng `0`.
- `PAYMENT_RECORD.payment_status` chỉ nhận `UNPAID`, `PARTIAL`, hoặc `PAID`.
- `PAYMENT_RECORD` có owner check: chính xác một trong hai trường `med_record_id` hoặc `appointment_id` phải có giá trị.
- `PAYMENT_TRANSACTION.payment_record_id` tham chiếu `PAYMENT_RECORD.payment_record_id`.
- `PAYMENT_TRANSACTION.sepay_transaction_id` là duy nhất nếu có giá trị.
- `PAYMENT_TRANSACTION.confirmed_by_account_id` tham chiếu `ACCOUNT.account_id`.
- `PAYMENT_TRANSACTION.transfer_amount` phải lớn hơn `0`.
- `PAYMENT_TRANSACTION.process_status` chỉ nhận `PENDING`, `SUCCESS`, hoặc `FAILED`.
- Rule service: số tiền thanh toán appointment phải khớp chính xác với phí khám cần thu.
- Rule service: walk-in cash tạo `PAYMENT_TRANSACTION` gateway `CASH` kèm người thu/xác nhận.
- Rule service: webhook SePay tạo `PAYMENT_TRANSACTION` gateway `SEPAY` từ dữ liệu gateway trả về.
- Rule API: `PaymentRecordController` chỉ đọc; payment record và transaction được sinh bởi business flow.
