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

Project xử lý trong `MedicalRecordServiceImpl.complete(...)` và `MedicalRecordWorkflowServiceImpl.completeIfReady(...)` bằng `@Transactional`. Service lock `MedicalRecord` bằng repository method có `PESSIMISTIC_WRITE`, kiểm tra MR đang `IN_PROGRESS`, kiểm tra các trường clinical bắt buộc và kiểm tra toàn bộ request liên quan đã `RESULT_AVAILABLE`. Sau đó service cập nhật `MedicalRecord.status = COMPLETED`, set `completedAt`, đồng thời set `appointment.status = COMPLETED` trong cùng transaction.

## 12. Tạo Request Xét Nghiệm/Dịch Vụ Và Sync Billing

### Vấn đề

Loại vấn đề: `lost update`, `stale read`, `inconsistent write`.

Bác sĩ có thể tạo thêm lab test request hoặc medical service request trong lúc luồng khác đang chuẩn bị ghi nhận thanh toán medical record. Nếu không lock medical record, payment có thể đọc `total_price` cũ, còn request mới vừa được thêm sau đó. Khi đó bệnh nhân thanh toán thiếu so với tổng dịch vụ đã chỉ định.

Ngoài ra, tạo request ghi nhiều bảng cùng lúc: request, request items, `MEDICAL_RECORD.total_price`, và `PAYMENT_RECORD.total_price`. Nếu một phần commit còn phần khác lỗi, dữ liệu billing sẽ lệch.

### Cách giải quyết

Project xử lý trong `LabTestRequestServiceImpl.createRequest(...)` và `MedicalServiceRequestServiceImpl.createRequest(...)` bằng `@Transactional`. Service lock `MedicalRecord` qua `MedicalRecordRepository.findByIdForUpdate(...)`, chỉ cho tạo request khi MR đang `DRAFT`, lưu request/items, rồi gọi `MedicalRecordBillingService.syncBilling(...)` để tính lại tổng lab/service và sync sang payment record trong cùng transaction.

## 13. Thanh Toán Medical Record

### Vấn đề

Loại vấn đề: `stale read`, `double payment`, `lost update`, `inconsistent write`.

Hai nhân viên có thể cùng ghi nhận thanh toán một medical record, hoặc thanh toán xảy ra song song với việc bác sĩ thêm request. Nếu không khóa dữ liệu, một giao dịch có thể đọc tổng tiền cũ, hoặc cả hai giao dịch cùng thấy payment chưa paid rồi cùng tạo `PaymentTransaction`.

Thanh toán medical record còn phải đổi trạng thái MR từ `DRAFT` sang `IN_PROGRESS`. Nếu payment đã ghi nhưng MR chưa đổi trạng thái, request result sẽ bị kẹt vì flow chỉ cho cập nhật request khi MR `IN_PROGRESS` và đã paid.

### Cách giải quyết

Project xử lý trong `PaymentRecordServiceImpl.recordMedicalRecordCashPayment(...)` bằng `@Transactional`. Service lock `MedicalRecord`, sync billing lại ngay trước khi thu tiền, lock `PaymentRecord` bằng `findByMedicalRecordIdForUpdate(...)`, kiểm tra số tiền thu khớp chính xác tổng tiền, tạo `PaymentTransaction` gateway `CASH`, set payment `PAID`, rồi chuyển MR sang `IN_PROGRESS` trong cùng transaction.

## 14. Cập Nhật Request Và Tạo Result

### Vấn đề

Loại vấn đề: `lost update`, `duplicate result`, `invalid state transition`, `non-repeatable read`.

Kỹ thuật viên có thể cập nhật cùng một request ở nhiều tab hoặc hai người cùng thao tác trên một request. Nếu không lock, một request có thể bị chuyển trạng thái sai thứ tự, ví dụ tạo result khi chưa lấy mẫu, hoặc tạo hai result cho cùng một request.

Ngoài ra, nếu MR chưa được thanh toán mà request vẫn được cập nhật, flow nghiệp vụ sẽ bị bỏ qua bước bệnh nhân thanh toán.

### Cách giải quyết

Project xử lý trong `LabTestRequestServiceImpl` và `MedicalServiceRequestServiceImpl` bằng transaction. Khi cập nhật status hoặc tạo result, service lock request bằng `findByIdForUpdate(...)`, validate MR liên quan đang `IN_PROGRESS` và payment record của MR đã `PAID`. Cập nhật trạng thái thủ công chỉ cho phép sang `SAMPLE_COLLECTED`; tạo result yêu cầu request đang `SAMPLE_COLLECTED` và sau đó set request `RESULT_AVAILABLE`.

Database cũng có quan hệ 1-1 giữa request và result (`LAB_TEST_RESULT.lab_test_request_id` unique, `MEDICAL_SERVICE_RESULT.med_ser_req_id` unique) để chặn result trùng ở tầng DB.

## 15. Auto-complete Medical Record Sau Khi Request Hoàn Thành

### Vấn đề

Loại vấn đề: `inconsistent write`, `lost update`, `race condition`.

Khi request cuối cùng có result, hệ thống cần hoàn tất medical record và appointment liên quan. Nếu hai result hoàn tất gần như đồng thời, cả hai luồng có thể cùng kiểm tra điều kiện complete và cùng cập nhật MR/appointment.

Nếu complete MR không đi chung với complete appointment, dữ liệu có thể lệch: MR đã hoàn tất nhưng appointment vẫn `IN_PROGRESS`.

### Cách giải quyết

Sau khi tạo result, service gọi `MedicalRecordWorkflowService.completeIfReady(...)`. Workflow service lock `MedicalRecord`, kiểm tra MR vẫn `IN_PROGRESS`, kiểm tra các trường clinical bắt buộc, kiểm tra toàn bộ lab/service request đều `RESULT_AVAILABLE`, rồi set MR và appointment sang `COMPLETED` trong cùng transaction.

## 16. Báo Cáo Doanh Thu

### Vấn đề

Loại vấn đề: `dirty read`, `inconsistent read`.

Báo cáo doanh thu phải chỉ tính giao dịch thanh toán đã commit thành công. Nếu query đọc từ payment record chưa paid hoặc transaction chưa success, số liệu có thể lệch với kế toán.

### Cách giải quyết

Project xử lý trong `RevenueReportServiceImpl.getRevenueReport(...)` bằng read-only transaction. Báo cáo chỉ đọc `PAYMENT_TRANSACTION` có `process_status = SUCCESS`, join sang `PAYMENT_RECORD` để suy ra owner type `APPOINTMENT` hoặc `MEDICAL_RECORD`, và không tự ghi dữ liệu kế toán. Flow ghi payment vẫn chịu trách nhiệm transaction/lock trước khi commit, nên báo cáo chỉ nhìn thấy dữ liệu đã ổn định sau commit.

## 17. Tạo Lịch Làm Việc Bác Sĩ

### Vấn đề

Loại vấn đề: `phantom read`, `duplicate insert race`.

Hai admin có thể cùng tạo lịch cho một bác sĩ trong cùng ngày/cùng ca, hoặc cùng gán một phòng cho hai bác sĩ trong cùng ca. Cả hai transaction có thể cùng query không thấy lịch trùng, rồi cùng insert. Đây là `phantom read` dẫn đến `duplicate insert race`.

### Cách giải quyết

Project có unique constraint ở database cho `(doctor_id, schedule_date, shift)` và `(room_id, schedule_date, shift)`. Trong `DoctorScheduleServiceImpl.create(...)`, service vẫn validate trước, nhưng khi save dùng `saveAndFlush(...)` và bắt `DataIntegrityViolationException` trong `saveWithDuplicateHandling(...)` để trả lỗi nghiệp vụ rõ ràng nếu có race condition.

## 18. Cập Nhật Lịch Làm Việc Bác Sĩ

### Vấn đề

Loại vấn đề: `lost update`, `write skew`.

Admin có thể giảm `maxCapacity` trong lúc booking đang tăng. Transaction update lịch có thể đọc `currentBookingCount` cũ, trong khi transaction booking tăng counter sau đó hoặc song song. Kết quả có thể làm `maxCapacity` nhỏ hơn số lượng đã đặt. Đây là `write skew`.

Nếu hai admin cùng sửa một lịch, request sau có thể ghi đè request trước. Đây là `lost update`.

### Cách giải quyết

Project kiểm tra trong `DoctorScheduleServiceImpl.update(...)`: `maxCapacity` mới không được nhỏ hơn `currentBookingCount`. Entity `DoctorSchedule` cũng có `@Version` qua `versionNumber`, giúp phát hiện update ghi đè khi nhiều transaction cùng sửa cùng một lịch.

## 19. Optimistic Locking Cho Appointment Và DoctorSchedule

### Vấn đề

Loại vấn đề: `lost update`.

Ngoài các flow đã dùng pessimistic lock, vẫn có khả năng một entity bị nhiều request cập nhật cùng lúc từ các luồng khác nhau. Nếu không có version, request commit sau có thể ghi đè thay đổi của request commit trước. Đây là `lost update`.

### Cách giải quyết

Project thêm `versionNumber` và `@Version` cho `Appointment` và `DoctorSchedule`. Migration `V6__add_version_columns_for_optimistic_locking.sql` thêm cột `version_number` cho hai bảng này. Khi Hibernate phát hiện version đã thay đổi, nó sẽ throw optimistic locking exception thay vì ghi đè dữ liệu.

## 20. Unique Constraint Cho Các Dữ Liệu Dễ Bị Tạo Trùng

### Vấn đề

Loại vấn đề: `phantom read`, `duplicate insert race`.

Các dữ liệu như email account, identity number, license number, role name, permission name, appointment code, request code có thể bị tạo trùng khi nhiều request chạy đồng thời. Nếu service chỉ check bằng `existsBy...`, hai transaction có thể cùng đọc là chưa tồn tại rồi cùng insert. Đây là `phantom read` dẫn đến `duplicate insert race`.

### Cách giải quyết

Project đặt unique constraint ở database cho các field quan trọng. Service có thể check trước bằng `existsBy...`, nhưng tầng bảo vệ cuối cùng vẫn là database constraint. Một số service bắt `DataIntegrityViolationException` để đổi lỗi DB thành lỗi nghiệp vụ dễ hiểu hơn.

## 21. Cấp Phát Đơn Thuốc Và Trừ Kho 

### Vấn đề

Loại vấn đề: `lost update`, `write skew`, `race condition`, `inconsistent write`.

Khi nhiều Dược sĩ cùng thực hiện cấp phát đơn thuốc gần như đồng thời cho các đơn chứa chung loại thuốc, hoặc một Bác sĩ đang gửi đơn thuốc (kê đơn) chứa loại thuốc đó trong khi Dược sĩ khác đang cấp phát.

Nếu không khóa dữ liệu:
1. Hai giao dịch cấp phát song song có thể cùng đọc số lượng tồn kho của các lô thuốc và cùng ghi đè giá trị trừ kho của nhau (`lost update`), khiến số lượng tồn thực tế trong database cao hơn/thấp hơn thực tế hoặc gây tồn kho âm.
2. Bác sĩ kê đơn kiểm tra tồn kho vẫn đủ, nhưng cùng lúc Dược sĩ vừa hoàn thành trừ kho cấp phát, dẫn đến số lượng thực tế trong kho không còn đủ cho đơn vừa kê (`write skew`).
3. Dữ liệu trừ kho không nhất quán (`inconsistent write`): một số thuốc được trừ thành công nhưng thuốc sau bị thiếu và giao dịch không được rollback toàn bộ.

### Cách giải quyết

Project xử lý trong `PrescriptionServiceImpl.dispensePrescription(...)` bằng `@Transactional`.

Khi bắt đầu cấp phát, với mỗi loại thuốc có trong đơn, service sẽ:
1. Gọi `MedicineLotRepository.findAllByMedicineIdAndIsActiveForUpdate(medicineId, 1)` sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` để khóa ghi tất cả các lô thuốc đang hoạt động của thuốc đó tại tầng database. Việc này ngăn chặn các transaction kê đơn (trong `validatePrescriptionDetails`) hoặc cấp phát khác truy cập đồng thời vào các lô thuốc của dược phẩm này.
2. Kiểm tra tổng tồn kho khả dụng real-time (bằng cách loại bỏ các lô đã hết hạn). Nếu không đủ số lượng, lập tức throw `BusinessException` để rollback toàn bộ giao dịch.
3. Áp dụng thuật toán FIFO: sắp xếp các lô khả dụng theo ngày hết hạn `expiryDate` tăng dần và tiến hành trừ kho an toàn.
4. Cập nhật trạng thái đơn thuốc sang đã cấp phát (`isActive = 0`).

Tương tự, tại luồng kê đơn (`PrescriptionServiceImpl.createPrescription`), service cũng gọi `validatePrescriptionDetails` có sử dụng Pessimistic Lock tương tự để kiểm tra tồn kho khả dụng trước khi cho phép bác sĩ kê đơn thành công, loại bỏ hoàn toàn khả năng kê đơn vượt quá tồn kho thực tế.

## 22. Ngừng Kinh Doanh Thuốc (Medicine SKU Deactivation)

### Vấn đề

Loại vấn đề: `lost update`, `write skew`, `race condition`, `inconsistent write`.

Theo quy định nghiệp vụ, Dược sĩ hoặc Admin chỉ được phép ngừng kinh doanh (deactive) một loại thuốc (Medicine SKU) khi tổng số lượng tồn kho khả dụng của tất cả các lô thuốc đang hoạt động của thuốc đó nhỏ hơn hoặc bằng 10 (`totalStock <= 10`).

Nếu không khóa dữ liệu trước khi kiểm tra:
1. Tranh chấp đồng thời (race condition) xảy ra giữa một Admin thực hiện ngừng kinh doanh thuốc này, và một Dược sĩ đang đồng thời thực hiện nhập thêm lô thuốc mới (tăng số lượng) hoặc cập nhật số lượng tồn kho của một lô thuốc hiện tại qua giao diện kho.
2. Admin đọc thấy tổng tồn kho cũ là 8 (thỏa mãn điều kiện <= 10), trong khi cùng lúc Dược sĩ ghi nhận nhập lô mới 100 viên và commit thành công. Admin tiếp tục ghi đè trạng thái thuốc thành ngừng kinh doanh (`isActive = 0`) và commit, dẫn đến việc ngừng kinh doanh một loại thuốc vẫn còn 108 viên tồn kho thực tế, vi phạm nghiêm trọng ràng buộc nghiệp vụ (`write skew` / `lost update` trạng thái ràng buộc).

### Cách giải quyết

Project giải quyết triệt để trong `MedicineServiceImpl.deactivateMedicine(...)` bằng `@Transactional` kết hợp Khóa Bi quan (Pessimistic Lock).

Khi bắt đầu deactive thuốc:
1. Service thực hiện lock ghi toàn bộ các lô thuốc đang hoạt động của thuốc đó tại tầng database bằng cách gọi `MedicineLotRepository.findAllByMedicineIdAndIsActiveForUpdate(id, 1)`. Phương thức này sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` khóa tất cả các dòng dữ liệu liên quan trong bảng `MedicineLot`.
2. Bất kỳ giao dịch nhập lô mới hoặc chỉnh sửa số lượng lô của thuốc này sẽ bị chặn và phải chờ cho đến khi transaction của Admin hoàn thành.
3. Tính toán tổng tồn kho thực tế của các lô thuốc đã khóa an toàn. Nếu tổng tồn kho vượt quá 10, lập tức ném ra lỗi `BusinessException` để rollback toàn bộ giao dịch, đảm bảo không có trạng thái không nhất quán.
4. Nếu tổng tồn kho hợp lệ (<= 10), cập nhật `isActive = 0` trên entity `Medicine` và lưu lại thông qua `MedicineRepository.save(...)`.
