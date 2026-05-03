# Các Chỗ Đang Xử Lý Concurrency Trong Project

Tài liệu này chỉ ra các điểm trong project hiện đang có xử lý concurrency. Mỗi mục gồm hai phần: vấn đề cần tránh và cách project đang giải quyết.

## 1. Đặt Lịch Khám Online

### Vấn đề

Khi nhiều người cùng đặt lịch vào một ca khám (`DoctorSchedule`), hệ thống có thể bị overbooking, cấp trùng số thứ tự (`queue_num`), hoặc tạo nhiều lịch active cho cùng một bệnh nhân.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.create(...)` bằng `@Transactional`. Khi tạo lịch, service lock bệnh nhân bằng `PatientRepository.findByIdForUpdate(...)` và lock ca khám bằng `DoctorScheduleRepository.findByIdForUpdate(...)` với `PESSIMISTIC_WRITE`. Sau đó mới kiểm tra sức chứa, tăng `currentBookingCount`, tăng `lastQueueNumber`, cấp `queue_num`, tạo appointment và tạo `PaymentRecord` trong cùng một transaction.

Ngoài ra, database có unique constraint `(doctor_schedule_id, queue_num)` để chặn trùng số thứ tự ở tầng DB.

## 2. Đặt Lịch Walk-in Đã Thanh Toán

### Vấn đề

Nhân viên có thể tạo lịch walk-in cùng lúc cho cùng một ca khám, dẫn đến vượt sức chứa hoặc lệch dữ liệu giữa appointment, payment record và payment transaction tiền mặt.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.createWalkInPaidAppointment(...)` bằng `@Transactional`. Flow này cũng lock `Patient` và `DoctorSchedule` trước khi cấp slot. Appointment được tạo ở trạng thái `CHECKED_IN`, đồng thời tạo `PaymentRecord` và `PaymentTransaction` gateway `CASH` trong cùng transaction. Nếu một bước lỗi, toàn bộ flow rollback.

## 3. Cấp Và Release Slot Của Ca Khám

### Vấn đề

Khi appointment bị hủy hoặc hết hạn thanh toán, hệ thống phải trả lại slot. Nếu nhiều request cùng release một slot, `current_booking_count` có thể bị giảm sai.

### Cách giải quyết

Project chỉ release slot trong transaction. Khi release, service gọi `findDoctorScheduleForUpdateOrThrow(...)` để lock `DoctorSchedule` rồi mới giảm `currentBookingCount`. `lastQueueNumber` không bị giảm để tránh cấp lại số thứ tự cũ.

Các flow liên quan nằm trong `AppointmentServiceImpl.cancel(...)` và `AppointmentServiceImpl.expirePendingPaymentReservations(...)`.

## 4. Hủy Appointment

### Vấn đề

Một appointment có thể bị hủy trong khi luồng khác đang check-in, start hoặc confirm payment. Nếu không lock, trạng thái cuối có thể bị ghi đè hoặc slot bị release sai.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.cancel(...)` bằng `@Transactional`. Appointment được lock bằng `AppointmentRepository.findByIdForUpdate(...)` với `PESSIMISTIC_WRITE`. Sau khi lock, service mới kiểm tra trạng thái hiện tại và chỉ cho hủy appointment đang `PENDING`. Appointment đã thanh toán hoặc đã đi vào quy trình khám không được hủy.

## 5. Check-in Appointment

### Vấn đề

Nhiều request có thể cùng check-in một appointment, hoặc check-in xảy ra đồng thời với cancel/start.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.checkIn(...)`. Service lock appointment bằng `findByIdForUpdate(...)`, sau đó chỉ cho chuyển trạng thái từ `CONFIRMED` sang `CHECKED_IN`. Vì appointment bị lock trong transaction, chỉ một request được đổi trạng thái tại một thời điểm.

## 6. Start Appointment

### Vấn đề

Bác sĩ hoặc hệ thống có thể gửi nhiều request start cùng một appointment, hoặc start trong lúc appointment đang bị luồng khác thay đổi trạng thái.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.start(...)`. Service lock appointment bằng `findByIdForUpdate(...)`, sau đó chỉ cho chuyển trạng thái từ `CHECKED_IN` sang `IN_PROGRESS`. Các request song song phải chờ lock và sẽ thấy trạng thái mới nhất sau khi transaction trước commit.

## 7. Xác Nhận Thanh Toán Qua SePay Webhook

### Vấn đề

Webhook từ SePay có thể được gửi lặp lại, gửi đồng thời, hoặc hai webhook khác nhau cùng cố confirm một appointment. Nếu không xử lý concurrency, hệ thống có thể tạo trùng transaction, confirm appointment đã hết hạn, hoặc cập nhật appointment/payment record lệch nhau.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.confirmPaymentFromSepayWebhook(...)` bằng `@Transactional`. Service kiểm tra idempotency qua `PaymentTransactionRepository.findBySepayTransactionId(...)`. Sau đó lock appointment bằng `AppointmentRepository.findByAppointmentCodeForUpdate(...)`, lock payment record bằng `PaymentRecordRepository.findByAppointmentIdForUpdate(...)`, rồi cập nhật appointment, payment record và tạo payment transaction trong cùng transaction.

Database cũng có unique constraint cho `Appointment.sepayTransactionId` và `PaymentTransaction.sepayTransactionId`. Khi save, project dùng `saveAndFlush(...)` và bắt `DataIntegrityViolationException` để phát hiện conflict sớm.

## 8. Scheduler Hết Hạn Thanh Toán

### Vấn đề

Scheduler có thể expire appointment cùng lúc webhook thanh toán đang confirm. Nếu hai luồng cùng xử lý appointment pending, có thể vừa confirm payment vừa release slot.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.expirePendingPaymentReservations(...)`. Scheduler lấy danh sách appointment hết hạn bằng `AppointmentRepository.findExpiredPaymentReservationsForUpdate(...)`, query này dùng `PESSIMISTIC_WRITE`. Sau khi lock appointment, service release slot và chuyển appointment sang `PAYMENT_EXPIRED` trong cùng transaction.

## 9. Tạo Medical Record Từ Appointment

### Vấn đề

Hai request có thể cùng tạo medical record cho một appointment, dẫn đến trùng hồ sơ khám.

### Cách giải quyết

Project xử lý trong `MedicalRecordServiceImpl.createFromAppointment(...)` bằng `@Transactional`. Service kiểm tra appointment phải đang `IN_PROGRESS`, kiểm tra chưa có medical record, sau đó save bằng `saveAndFlush(...)`. Database có unique constraint trên `MEDICAL_RECORD.appointment_id`; nếu hai request cùng pass validate, constraint DB vẫn chặn và service bắt `DataIntegrityViolationException` để trả lỗi duplicate.

## 10. Cập Nhật Medical Record

### Vấn đề

Bác sĩ có thể mở nhiều tab hoặc nhiều request cùng cập nhật một medical record. Request cũ có thể ghi đè nội dung mới hơn.

### Cách giải quyết

Project dùng optimistic locking cho `MedicalRecord` qua field `versionNumber` với `@Version`. Trong `MedicalRecordServiceImpl.update(...)`, client phải gửi `version`. Service so sánh version request với version hiện tại; nếu lệch thì throw `ObjectOptimisticLockingFailureException`. `GlobalExceptionHandler` trả lỗi này thành HTTP `409 CONFLICT`.

## 11. Complete Medical Record Và Complete Appointment

### Vấn đề

Khi hoàn tất hồ sơ khám, appointment liên quan cũng phải chuyển sang `COMPLETED`. Nếu hai thao tác này không atomic, có thể xảy ra tình trạng medical record completed nhưng appointment vẫn chưa completed.

### Cách giải quyết

Project xử lý trong `MedicalRecordServiceImpl.complete(...)` bằng `@Transactional`. Service cập nhật `MedicalRecord.status = COMPLETED`, set `completedAt`, đồng thời set `appointment.status = COMPLETED` trong cùng transaction.

## 12. Tạo Lịch Làm Việc Bác Sĩ

### Vấn đề

Hai admin có thể cùng tạo lịch cho một bác sĩ trong cùng ngày/cùng ca, hoặc cùng gán một phòng cho hai bác sĩ trong cùng ca. Validate ở service không đủ vì hai request có thể cùng pass validate trước khi save.

### Cách giải quyết

Project có unique constraint ở database cho `(doctor_id, schedule_date, shift)` và `(room_id, schedule_date, shift)`. Trong `DoctorScheduleServiceImpl.create(...)`, service vẫn validate trước, nhưng khi save dùng `saveAndFlush(...)` và bắt `DataIntegrityViolationException` trong `saveWithDuplicateHandling(...)` để trả lỗi nghiệp vụ rõ ràng nếu có race condition.

## 13. Cập Nhật Lịch Làm Việc Bác Sĩ

### Vấn đề

Admin có thể giảm `maxCapacity` trong lúc booking đang tăng, khiến sức chứa nhỏ hơn số lượng đã đặt.

### Cách giải quyết

Project kiểm tra trong `DoctorScheduleServiceImpl.update(...)`: `maxCapacity` mới không được nhỏ hơn `currentBookingCount`. Entity `DoctorSchedule` cũng có `@Version` qua `versionNumber`, giúp phát hiện update ghi đè khi nhiều transaction cùng sửa cùng một lịch.

## 14. Optimistic Locking Cho Appointment Và DoctorSchedule

### Vấn đề

Ngoài các flow đã dùng pessimistic lock, vẫn có khả năng một entity bị nhiều request cập nhật cùng lúc từ các luồng khác nhau.

### Cách giải quyết

Project thêm `versionNumber` và `@Version` cho `Appointment` và `DoctorSchedule`. Migration `V6__add_version_columns_for_optimistic_locking.sql` thêm cột `version_number` cho hai bảng này. Khi Hibernate phát hiện version đã thay đổi, nó sẽ throw optimistic locking exception thay vì ghi đè dữ liệu.

## 15. Unique Constraint Cho Các Dữ Liệu Dễ Bị Tạo Trùng

### Vấn đề

Các dữ liệu như email account, identity number, license number, role name, permission name, appointment code, request code có thể bị tạo trùng khi nhiều request chạy đồng thời.

### Cách giải quyết

Project đặt unique constraint ở database cho các field quan trọng. Service có thể check trước bằng `existsBy...`, nhưng tầng bảo vệ cuối cùng vẫn là database constraint. Một số service bắt `DataIntegrityViolationException` để đổi lỗi DB thành lỗi nghiệp vụ dễ hiểu hơn.
