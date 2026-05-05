# Các Chỗ Đang Xử Lý Concurrency Trong Project

Tài liệu này chỉ ra các điểm trong project hiện đang có xử lý concurrency. Mỗi mục gồm hai phần: vấn đề và cách giải quyết.

## 1. Đặt Lịch Khám Online

### Vấn đề

Loại vấn đề: `lost update`, `write skew`, `phantom read`, `duplicate insert race`.

Khi nhiều người cùng đặt lịch vào một ca khám (`DoctorSchedule`), các request có thể cùng đọc thấy `current_booking_count` vẫn còn chỗ rồi cùng ghi tăng số lượng. Đây là `lost update` nếu các transaction ghi đè counter của nhau, và là `write skew` nếu mỗi transaction đều thấy điều kiện sức chứa hợp lệ nhưng tổng kết quả sau commit lại vượt `max_capacity`.

Ngoài ra, nếu kiểm tra lịch active của bệnh nhân chỉ bằng query đọc thông thường, hai request song song có thể cùng không thấy appointment active nào rồi cùng tạo mới. Trường hợp này là `phantom read`/`duplicate insert race`.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.create(...)` bằng `@Transactional`. Khi tạo lịch, service lock bệnh nhân bằng `PatientRepository.findByIdForUpdate(...)` và lock ca khám bằng `DoctorScheduleRepository.findByIdForUpdate(...)` với `PESSIMISTIC_WRITE`. Sau đó mới kiểm tra sức chứa, tăng `currentBookingCount`, tăng `lastQueueNumber`, cấp `queue_num`, tạo appointment và tạo `PaymentRecord` trong cùng một transaction.

Database có unique constraint `(doctor_schedule_id, queue_num)` để chặn trùng số thứ tự ở tầng DB.

## 2. Đặt Lịch Walk-in Đã Thanh Toán

### Vấn đề

Loại vấn đề: `lost update`, `write skew`, `inconsistent write`.

Nhân viên có thể tạo lịch walk-in cùng lúc cho cùng một ca khám. Nếu không lock, nhiều transaction có thể cùng đọc sức chứa còn trống rồi cùng tăng `current_booking_count`, dẫn đến vượt sức chứa. Đây là `lost update` hoặc `write skew`.

Flow walk-in còn ghi nhiều bảng cùng lúc: `Appointment`, `PaymentRecord`, `PaymentTransaction`. Nếu một phần ghi thành công nhưng phần khác lỗi, dữ liệu sẽ rơi vào trạng thái không nhất quán. Đây là `inconsistent write` do thiếu transaction atomic.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.createWalkInPaidAppointment(...)` bằng `@Transactional`. Flow này lock `Patient` và `DoctorSchedule` trước khi cấp slot. Appointment được tạo ở trạng thái `CHECKED_IN`, đồng thời tạo `PaymentRecord` và `PaymentTransaction` gateway `CASH` trong cùng transaction. Nếu một bước lỗi, toàn bộ flow rollback.

## 3. Cấp Và Release Slot Của Ca Khám

### Vấn đề

Loại vấn đề: `lost update`, `double decrement`, `inconsistent write`.

Khi appointment bị hủy hoặc hết hạn thanh toán, hệ thống phải trả lại slot bằng cách giảm `current_booking_count`. Nếu hai request cùng release một appointment hoặc cùng sửa một `DoctorSchedule`, counter có thể bị giảm sai. Đây là dạng `lost update` hoặc `double decrement`.

Nếu appointment đã đổi trạng thái nhưng slot chưa release, hoặc slot đã release nhưng appointment chưa đổi trạng thái, dữ liệu bị lệch giữa appointment và schedule. Đây là `inconsistent write`.

### Cách giải quyết

Project chỉ release slot trong transaction. Khi release, service gọi `findDoctorScheduleForUpdateOrThrow(...)` để lock `DoctorSchedule` rồi mới giảm `currentBookingCount`. `lastQueueNumber` không bị giảm để tránh cấp lại số thứ tự cũ.

Các flow liên quan nằm trong `AppointmentServiceImpl.cancel(...)` và `AppointmentServiceImpl.expirePendingPaymentReservations(...)`.

## 4. Hủy Appointment

### Vấn đề

Loại vấn đề: `non-repeatable read`, `lost update`, `race condition`.

Một appointment có thể bị hủy trong khi luồng khác đang check-in, start hoặc confirm payment. Nếu một transaction đọc appointment là `PENDING`, sau đó transaction khác đổi sang `CONFIRMED` hoặc `CHECKED_IN`, request cũ có thể vẫn tiếp tục hủy dựa trên dữ liệu cũ. Đây là `non-repeatable read` dẫn đến `lost update` trạng thái.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.cancel(...)` bằng `@Transactional`. Appointment được lock bằng `AppointmentRepository.findByIdForUpdate(...)` với `PESSIMISTIC_WRITE`. Sau khi lock, service mới kiểm tra trạng thái hiện tại và chỉ cho hủy appointment đang `PENDING`. Appointment đã thanh toán hoặc đã đi vào quy trình khám không được hủy.

## 5. Check-in Appointment

### Vấn đề

Loại vấn đề: `lost update`, `non-repeatable read`, `race condition`.

Nhiều request có thể cùng check-in một appointment, hoặc check-in xảy ra đồng thời với cancel/start. Nếu không lock, request có thể đọc trạng thái cũ rồi ghi đè trạng thái mới hơn. Đây là `lost update` trên trạng thái appointment.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.checkIn(...)`. Service lock appointment bằng `findByIdForUpdate(...)`, sau đó chỉ cho chuyển trạng thái từ `CONFIRMED` sang `CHECKED_IN`. Vì appointment bị lock trong transaction, chỉ một request được đổi trạng thái tại một thời điểm.

## 6. Start Appointment

### Vấn đề

Loại vấn đề: `lost update`, `non-repeatable read`, `race condition`.

Bác sĩ hoặc hệ thống có thể gửi nhiều request start cùng một appointment, hoặc start trong lúc appointment đang bị luồng khác thay đổi trạng thái. Nếu không lock, request sau có thể ghi dựa trên trạng thái không còn đúng. Đây là `non-repeatable read` và có thể dẫn đến `lost update`.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.start(...)`. Service lock appointment bằng `findByIdForUpdate(...)`, sau đó chỉ cho chuyển trạng thái từ `CHECKED_IN` sang `IN_PROGRESS`. Các request song song phải chờ lock và sẽ thấy trạng thái mới nhất sau khi transaction trước commit.

## 7. Xác Nhận Thanh Toán Qua SePay Webhook

### Vấn đề

Loại vấn đề: `duplicate processing`, `lost update`, `non-repeatable read`, `idempotency race`.

Webhook từ SePay có thể được gửi lặp lại hoặc gửi đồng thời. Nếu không xử lý idempotency, cùng một giao dịch có thể tạo nhiều `PaymentTransaction`. Đây là `duplicate processing` và `idempotency race`.

Ngoài ra, hai webhook khác nhau có thể cùng cố confirm một appointment. Nếu không lock, cả hai có thể cùng đọc appointment là `PENDING` rồi cùng ghi nhận thanh toán. Đây là `lost update`/`non-repeatable read`.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.confirmPaymentFromSepayWebhook(...)` bằng `@Transactional`. Service kiểm tra idempotency qua `PaymentTransactionRepository.findBySepayTransactionId(...)`. Sau đó lock appointment bằng `AppointmentRepository.findByAppointmentCodeForUpdate(...)`, lock payment record bằng `PaymentRecordRepository.findByAppointmentIdForUpdate(...)`, rồi cập nhật appointment, payment record và tạo payment transaction trong cùng transaction.

Database cũng có unique constraint cho `Appointment.sepayTransactionId` và `PaymentTransaction.sepayTransactionId`. Khi save, project dùng `saveAndFlush(...)` và bắt `DataIntegrityViolationException` để phát hiện conflict sớm.

## 8. Scheduler Hết Hạn Thanh Toán

### Vấn đề

Loại vấn đề: `race condition`, `lost update`, `non-repeatable read`.

Scheduler có thể expire appointment cùng lúc webhook thanh toán đang confirm. Một transaction có thể đọc appointment là `PENDING` để expire, trong khi transaction khác cũng đọc `PENDING` để confirm payment. Nếu không lock, kết quả cuối có thể vừa release slot vừa confirm thanh toán. Đây là `race condition` và `lost update` trên trạng thái appointment.

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.expirePendingPaymentReservations(...)`. Scheduler lấy danh sách appointment hết hạn bằng `AppointmentRepository.findExpiredPaymentReservationsForUpdate(...)`, query này dùng `PESSIMISTIC_WRITE`. Sau khi lock appointment, service release slot và chuyển appointment sang `PAYMENT_EXPIRED` trong cùng transaction.

## 9. Tạo Medical Record Từ Appointment

### Vấn đề

Loại vấn đề: `phantom read`, `duplicate insert race`.

Hai request có thể cùng kiểm tra chưa có medical record cho appointment, sau đó cùng tạo hồ sơ khám. Query kiểm tra ban đầu không thấy row nào, nhưng transaction khác có thể insert row mới ngay sau đó. Đây là `phantom read` dẫn đến `duplicate insert race`.

### Cách giải quyết

Project xử lý trong `MedicalRecordServiceImpl.createFromAppointment(...)` bằng `@Transactional`. Service kiểm tra appointment phải đang `IN_PROGRESS`, kiểm tra chưa có medical record, sau đó save bằng `saveAndFlush(...)`. Database có unique constraint trên `MEDICAL_RECORD.appointment_id`; nếu hai request cùng pass validate, constraint DB vẫn chặn và service bắt `DataIntegrityViolationException` để trả lỗi duplicate.

## 10. Cập Nhật Medical Record

### Vấn đề

Loại vấn đề: `lost update`.

Bác sĩ có thể mở nhiều tab hoặc nhiều request cùng cập nhật một medical record. Nếu request cũ submit sau request mới, nó có thể ghi đè nội dung mới hơn. Đây là `lost update`.

### Cách giải quyết

Project dùng optimistic locking cho `MedicalRecord` qua field `versionNumber` với `@Version`. Trong `MedicalRecordServiceImpl.update(...)`, client phải gửi `version`. Service so sánh version request với version hiện tại; nếu lệch thì throw `ObjectOptimisticLockingFailureException`. `GlobalExceptionHandler` trả lỗi này thành HTTP `409 CONFLICT`.

## 11. Complete Medical Record Và Complete Appointment

### Vấn đề

Loại vấn đề: `inconsistent write`, `lost update`.

Khi hoàn tất hồ sơ khám, appointment liên quan cũng phải chuyển sang `COMPLETED`. Nếu hai thao tác này không atomic, có thể xảy ra tình trạng medical record completed nhưng appointment vẫn chưa completed. Đây là `inconsistent write`.

Nếu trong cùng thời điểm có request khác cũng đổi appointment hoặc medical record, có thể xảy ra `lost update` trạng thái.

### Cách giải quyết

Project xử lý trong `MedicalRecordServiceImpl.complete(...)` bằng `@Transactional`. Service cập nhật `MedicalRecord.status = COMPLETED`, set `completedAt`, đồng thời set `appointment.status = COMPLETED` trong cùng transaction.

## 12. Tạo Lịch Làm Việc Bác Sĩ

### Vấn đề

Loại vấn đề: `phantom read`, `duplicate insert race`.

Hai admin có thể cùng tạo lịch cho một bác sĩ trong cùng ngày/cùng ca, hoặc cùng gán một phòng cho hai bác sĩ trong cùng ca. Cả hai transaction có thể cùng query không thấy lịch trùng, rồi cùng insert. Đây là `phantom read` dẫn đến `duplicate insert race`.

### Cách giải quyết

Project có unique constraint ở database cho `(doctor_id, schedule_date, shift)` và `(room_id, schedule_date, shift)`. Trong `DoctorScheduleServiceImpl.create(...)`, service vẫn validate trước, nhưng khi save dùng `saveAndFlush(...)` và bắt `DataIntegrityViolationException` trong `saveWithDuplicateHandling(...)` để trả lỗi nghiệp vụ rõ ràng nếu có race condition.

## 13. Cập Nhật Lịch Làm Việc Bác Sĩ

### Vấn đề

Loại vấn đề: `lost update`, `write skew`.

Admin có thể giảm `maxCapacity` trong lúc booking đang tăng. Transaction update lịch có thể đọc `currentBookingCount` cũ, trong khi transaction booking tăng counter sau đó hoặc song song. Kết quả có thể làm `maxCapacity` nhỏ hơn số lượng đã đặt. Đây là `write skew`.

Nếu hai admin cùng sửa một lịch, request sau có thể ghi đè request trước. Đây là `lost update`.

### Cách giải quyết

Project kiểm tra trong `DoctorScheduleServiceImpl.update(...)`: `maxCapacity` mới không được nhỏ hơn `currentBookingCount`. Entity `DoctorSchedule` cũng có `@Version` qua `versionNumber`, giúp phát hiện update ghi đè khi nhiều transaction cùng sửa cùng một lịch.

## 14. Optimistic Locking Cho Appointment Và DoctorSchedule

### Vấn đề

Loại vấn đề: `lost update`.

Ngoài các flow đã dùng pessimistic lock, vẫn có khả năng một entity bị nhiều request cập nhật cùng lúc từ các luồng khác nhau. Nếu không có version, request commit sau có thể ghi đè thay đổi của request commit trước. Đây là `lost update`.

### Cách giải quyết

Project thêm `versionNumber` và `@Version` cho `Appointment` và `DoctorSchedule`. Migration `V6__add_version_columns_for_optimistic_locking.sql` thêm cột `version_number` cho hai bảng này. Khi Hibernate phát hiện version đã thay đổi, nó sẽ throw optimistic locking exception thay vì ghi đè dữ liệu.

## 15. Unique Constraint Cho Các Dữ Liệu Dễ Bị Tạo Trùng

### Vấn đề

Loại vấn đề: `phantom read`, `duplicate insert race`.

Các dữ liệu như email account, identity number, license number, role name, permission name, appointment code, request code có thể bị tạo trùng khi nhiều request chạy đồng thời. Nếu service chỉ check bằng `existsBy...`, hai transaction có thể cùng đọc là chưa tồn tại rồi cùng insert. Đây là `phantom read` dẫn đến `duplicate insert race`.

### Cách giải quyết

Project đặt unique constraint ở database cho các field quan trọng. Service có thể check trước bằng `existsBy...`, nhưng tầng bảo vệ cuối cùng vẫn là database constraint. Một số service bắt `DataIntegrityViolationException` để đổi lỗi DB thành lỗi nghiệp vụ dễ hiểu hơn.
