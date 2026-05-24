# Các Transaction Chính Trong Project

Tài liệu này tóm tắt các nhóm transaction cần có trong project và những hành động nằm trong từng transaction. Các nhóm này được rút ra từ `docs/CONCURRENCY_HANDLING.md`.

## 1. Transaction Đặt Lịch Online

### Flow liên quan

- `AppointmentServiceImpl.create(...)`

### Khi nào cần transaction

Cần transaction khi bệnh nhân đặt lịch online. Flow này vừa kiểm tra điều kiện nghiệp vụ, vừa giữ slot của ca khám, vừa tạo appointment và payment record ban đầu.

### Hành động trong transaction

- Lock `Patient` bằng `PatientRepository.findByIdForUpdate(...)`.
- Lock `DoctorSchedule` bằng `DoctorScheduleRepository.findByIdForUpdate(...)`.
- Kiểm tra bệnh nhân chưa có appointment active trùng điều kiện.
- Kiểm tra `currentBookingCount < maxCapacity`.
- Tăng `currentBookingCount`.
- Tăng `lastQueueNumber`.
- Gán `queueNum` mới cho appointment.
- Tạo `Appointment`.
- Tạo `PaymentRecord`.

### Lý do cần transaction

- Tránh nhiều request cùng giữ một slot làm vượt `maxCapacity`.
- Tránh cấp trùng `queueNum`.
- Đảm bảo appointment và payment record được tạo cùng nhau.
- Nếu một bước lỗi thì rollback toàn bộ, không để dữ liệu nửa vời.

## 2. Transaction Đặt Lịch Walk-in Đã Thanh Toán

### Flow liên quan

- `AppointmentServiceImpl.createWalkInPaidAppointment(...)`

### Khi nào cần transaction

Cần transaction khi nhân viên tạo lịch walk-in và ghi nhận bệnh nhân đã thanh toán tiền mặt. Flow này giữ slot, tạo appointment, tạo payment record và tạo payment transaction trong cùng một lần xử lý.

### Hành động trong transaction

- Lock `Patient` bằng `PatientRepository.findByIdForUpdate(...)`.
- Lock `DoctorSchedule` bằng `DoctorScheduleRepository.findByIdForUpdate(...)`.
- Kiểm tra bệnh nhân chưa có appointment active trùng điều kiện.
- Kiểm tra `currentBookingCount < maxCapacity`.
- Tăng `currentBookingCount`.
- Tăng `lastQueueNumber`.
- Gán `queueNum` mới cho appointment.
- Tạo `Appointment` ở trạng thái `CHECKED_IN`.
- Tạo `PaymentRecord` đã thanh toán.
- Tạo `PaymentTransaction` với gateway `CASH`.
- Lưu người thu hoặc người xác nhận thanh toán nếu có.

### Lý do cần transaction

- Tránh nhiều nhân viên cùng tạo lịch vào một ca làm vượt `maxCapacity`.
- Tránh cấp trùng `queueNum`.
- Đảm bảo appointment, payment record và cash transaction được commit cùng nhau.
- Nếu tạo appointment thành công nhưng tạo payment lỗi thì toàn bộ flow phải rollback.

## 3. Transaction Đổi Trạng Thái Appointment

### Flow liên quan

- `AppointmentServiceImpl.cancel(...)`
- `AppointmentServiceImpl.checkIn(...)`
- `AppointmentServiceImpl.start(...)`
- `AppointmentServiceImpl.expirePendingPaymentReservations(...)`

### Khi nào cần transaction

Cần transaction khi appointment chuyển qua các trạng thái quan trọng. Các request song song có thể cùng thao tác trên một appointment, vì vậy phải lock row trước khi kiểm tra và cập nhật status.

### Hành động trong transaction

- Lock `Appointment` bằng repository method có `PESSIMISTIC_WRITE`, ví dụ `findByIdForUpdate(...)`.
- Đọc trạng thái mới nhất của appointment sau khi lock.
- Kiểm tra trạng thái hiện tại có được phép chuyển tiếp không.
- Cập nhật status appointment theo đúng flow.

### Các chuyển trạng thái tiêu biểu

- `PENDING -> CANCELLED`
- `CONFIRMED -> CHECKED_IN`
- `CHECKED_IN -> IN_PROGRESS`
- `PENDING -> PAYMENT_EXPIRED`

### Lý do cần transaction

- Tránh cancel xảy ra đồng thời với check-in, start hoặc confirm payment.
- Tránh request cũ ghi đè trạng thái mới hơn.
- Đảm bảo mỗi appointment chỉ được xử lý bởi một request tại một thời điểm.

## 4. Transaction Release Slot

### Flow liên quan

- `AppointmentServiceImpl.cancel(...)`
- `AppointmentServiceImpl.expirePendingPaymentReservations(...)`

### Khi nào cần transaction

Cần transaction khi appointment bị hủy hoặc hết hạn thanh toán và slot đã giữ phải được trả lại cho `DoctorSchedule`.

### Hành động trong transaction

- Lock `Appointment` cần hủy hoặc expire.
- Kiểm tra appointment có đang ở trạng thái được phép release slot không.
- Lock `DoctorSchedule` bằng `findDoctorScheduleForUpdateOrThrow(...)`.
- Giảm `currentBookingCount`.
- Không giảm `lastQueueNumber` để tránh cấp lại số thứ tự cũ.
- Cập nhật appointment sang `CANCELLED` hoặc `PAYMENT_EXPIRED`.

### Lý do cần transaction

- Tránh hai request cùng release một appointment làm giảm slot hai lần.
- Tránh appointment đã đổi status nhưng slot chưa được release.
- Tránh slot đã release nhưng appointment vẫn còn ở trạng thái cũ.

## 5. Transaction Thanh Toán

### Flow liên quan

- `AppointmentServiceImpl.confirmPaymentFromSepayWebhook(...)`
- `AppointmentServiceImpl.createWalkInPaidAppointment(...)`
- `PaymentRecordServiceImpl.recordMedicalRecordCashPayment(...)`
- `RevenueReportServiceImpl.getRevenueReport(...)`

### Khi nào cần transaction

Cần transaction khi hệ thống ghi nhận thanh toán từ SePay webhook, từ tiền mặt walk-in, hoặc từ phiếu medical record sau khi bác sĩ chỉ định xét nghiệm/dịch vụ. Flow thanh toán cập nhật nhiều bảng và cần xử lý idempotency để tránh ghi nhận trùng giao dịch.

Báo cáo doanh thu dùng read-only transaction riêng vì chỉ tổng hợp dữ liệu từ các `PaymentTransaction` đã `SUCCESS`.

### Hành động trong transaction với SePay webhook

- Kiểm tra `sepayTransactionId` đã từng được xử lý chưa.
- Lock `Appointment` bằng `AppointmentRepository.findByAppointmentCodeForUpdate(...)`.
- Lock `PaymentRecord` bằng `PaymentRecordRepository.findByAppointmentIdForUpdate(...)`.
- Kiểm tra appointment còn đang chờ thanh toán.
- Cập nhật appointment sang trạng thái đã xác nhận, ví dụ `CONFIRMED`.
- Cập nhật `PaymentRecord` sang đã thanh toán.
- Lưu thông tin `sepayTransactionId`.
- Tạo `PaymentTransaction` gateway `SEPAY`.
- Dùng `saveAndFlush(...)` và unique constraint để phát hiện conflict sớm.

### Hành động trong transaction với walk-in cash

- Tạo appointment ở trạng thái `CHECKED_IN`.
- Tạo `PaymentRecord` đã thanh toán.
- Tạo `PaymentTransaction` gateway `CASH`.
- Lưu người thu hoặc người xác nhận thanh toán nếu có.

### Hành động trong transaction với medical record cash

- Lock `MedicalRecord` bằng `MedicalRecordRepository.findByIdForUpdate(...)`.
- Sync billing qua `MedicalRecordBillingService.syncBilling(...)` để tổng tiền luôn là tổng lab test request và medical service request hiện tại.
- Lock `PaymentRecord` bằng `PaymentRecordRepository.findByMedicalRecordIdForUpdate(...)`.
- Kiểm tra MR đang `DRAFT`, payment record chưa `PAID`, tổng tiền lớn hơn `0`, và số tiền thu khớp chính xác `totalPrice`.
- Tạo `PaymentTransaction` gateway `CASH`.
- Cập nhật `PaymentRecord` sang `PAID`.
- Chuyển `MedicalRecord.status` từ `DRAFT` sang `IN_PROGRESS`.

### Hành động trong read-only transaction với báo cáo doanh thu

- Đọc `PaymentTransaction` có `processStatus = SUCCESS`.
- Filter theo `transactionDate`, `gateway`, và owner type nếu request có truyền.
- Join sang `PaymentRecord` để xác định giao dịch thuộc appointment hay medical record.
- Tổng hợp doanh thu theo ngày, gateway và owner type; không ghi dữ liệu.

### Lý do cần transaction

- Tránh webhook bị gửi lặp tạo nhiều payment transaction.
- Tránh appointment vừa bị expire vừa được confirm thanh toán.
- Đảm bảo appointment, payment record và payment transaction luôn đồng bộ.
- Tránh thanh toán medical record đọc tổng tiền cũ trong lúc bác sĩ vừa thêm request.
- Đảm bảo payment record, payment transaction và trạng thái medical record commit cùng nhau.

## 6. Transaction Medical Record Và Doctor Schedule

Nhóm này gồm hai nghiệp vụ khác nhau, nhưng cùng cần transaction vì đều có thao tác đọc, kiểm tra điều kiện, rồi ghi lại dữ liệu quan trọng.

## 6.1. Medical Record

### Flow liên quan

- `MedicalRecordServiceImpl.createFromAppointment(...)`
- `MedicalRecordServiceImpl.update(...)`
- `MedicalRecordServiceImpl.complete(...)`
- `MedicalRecordServiceImpl.lock(...)`
- `LabTestRequestServiceImpl.createRequest(...)`
- `LabTestRequestServiceImpl.updateStatus(...)`
- `LabTestRequestServiceImpl.createResult(...)`
- `LabTestRequestServiceImpl.updateResult(...)`
- `MedicalServiceRequestServiceImpl.createRequest(...)`
- `MedicalServiceRequestServiceImpl.updateStatus(...)`
- `MedicalServiceRequestServiceImpl.createResult(...)`
- `MedicalServiceRequestServiceImpl.updateResult(...)`
- `MedicalRecordBillingServiceImpl.syncBilling(...)`
- `MedicalRecordWorkflowServiceImpl.completeIfReady(...)`

### Hành động trong transaction khi tạo medical record

- Kiểm tra appointment tồn tại.
- Kiểm tra appointment đang ở trạng thái `IN_PROGRESS`.
- Kiểm tra appointment chưa có medical record.
- Tạo `MedicalRecord`.
- Save bằng `saveAndFlush(...)`.
- Dựa vào unique constraint trên `MEDICAL_RECORD.appointment_id` để chặn tạo trùng khi có race condition.

### Hành động trong transaction khi cập nhật medical record

- Đọc medical record cần cập nhật.
- Kiểm tra version client gửi lên.
- Nếu version lệch thì throw optimistic locking exception.
- Nếu version đúng thì cập nhật nội dung medical record.

### Hành động trong transaction khi complete medical record

- Lock `MedicalRecord` bằng repository method có `PESSIMISTIC_WRITE`.
- Kiểm tra MR đang `IN_PROGRESS`.
- Kiểm tra các trường bắt buộc để hoàn tất hồ sơ.
- Kiểm tra toàn bộ lab test request và medical service request của MR đều `RESULT_AVAILABLE`.
- Cập nhật `MedicalRecord.status = COMPLETED`.
- Set `completedAt`.
- Cập nhật appointment liên quan sang `COMPLETED`.

### Hành động trong transaction khi tạo request xét nghiệm/dịch vụ

- Lock `MedicalRecord` bằng `findByIdForUpdate(...)`.
- Kiểm tra MR đang `DRAFT`.
- Kiểm tra danh mục xét nghiệm/dịch vụ active.
- Snapshot giá vào request item.
- Tính `totalPrice` của request.
- Lưu request và request items.
- Sync lại `MedicalRecord.totalPrice` và `PaymentRecord.totalPrice`.

### Hành động trong transaction khi cập nhật trạng thái request

- Lock request bằng repository method `findByIdForUpdate(...)`.
- Lock/validate MR liên quan đang `IN_PROGRESS` và payment record của MR đã `PAID`.
- Chỉ cho phép cập nhật thủ công sang `SAMPLE_COLLECTED`.
- Không cho sửa request đã `RESULT_AVAILABLE`.

### Hành động trong transaction khi tạo/cập nhật result

- Lock request trước khi kiểm tra trạng thái.
- Khi tạo result, request phải đang `SAMPLE_COLLECTED`.
- Lưu result và set request sang `RESULT_AVAILABLE` trong cùng transaction.
- Khi cập nhật result, vẫn validate MR đang `IN_PROGRESS` và đã thanh toán.
- Sau khi tạo result, gọi `MedicalRecordWorkflowService.completeIfReady(...)`.

### Hành động trong transaction khi auto-complete medical record

- Lock `MedicalRecord`.
- Kiểm tra MR vẫn `IN_PROGRESS`.
- Kiểm tra các trường clinical bắt buộc đã có.
- Kiểm tra tất cả request liên quan đều `RESULT_AVAILABLE`.
- Set MR `COMPLETED`, set `completedAt`, và set appointment `COMPLETED`.

### Hành động trong transaction khi lock medical record

- Kiểm tra medical record đang `COMPLETED`.
- Cập nhật status sang `LOCKED`.
- Set `lockedAt`.

### Lý do cần transaction

- Tránh tạo trùng medical record cho cùng một appointment.
- Tránh request cũ ghi đè nội dung medical record mới hơn.
- Đảm bảo complete medical record và complete appointment đi cùng nhau.
- Tạo/cập nhật request xét nghiệm hoặc dịch vụ phải khóa medical record trước khi sync billing để tránh thanh toán lấy tổng tiền cũ.
- Ghi nhận thanh toán medical record phải khóa medical record, sync lại billing, rồi mới kiểm tra số tiền thu.
- Tránh hai nhân viên cùng cập nhật một request làm nhảy trạng thái sai.
- Tránh tạo result trùng hoặc tạo result khi request chưa lấy mẫu/chưa thực hiện.
- Đảm bảo khi request cuối cùng hoàn thành thì MR và appointment được hoàn tất atomically.
- Báo cáo doanh thu dùng read-only transaction vì chỉ đọc `PAYMENT_TRANSACTION` thành công.

## 6.2. Doctor Schedule

### Flow liên quan

- `DoctorScheduleServiceImpl.create(...)`
- `DoctorScheduleServiceImpl.update(...)`

### Hành động trong transaction khi tạo doctor schedule

- Kiểm tra bác sĩ có bị trùng `scheduleDate` và `shift` không.
- Kiểm tra phòng có bị trùng `scheduleDate` và `shift` không.
- Tạo `DoctorSchedule`.
- Save bằng `saveAndFlush(...)`.
- Bắt `DataIntegrityViolationException` nếu unique constraint phát hiện race condition.

### Hành động trong transaction khi cập nhật doctor schedule

- Đọc doctor schedule cần cập nhật.
- Kiểm tra `maxCapacity` mới không nhỏ hơn `currentBookingCount`.
- Cập nhật thông tin lịch.
- Dùng `versionNumber` với `@Version` để tránh lost update khi nhiều admin cùng sửa.

### Lý do cần transaction

- Tránh tạo trùng lịch bác sĩ hoặc trùng phòng khi nhiều admin thao tác cùng lúc.
- Tránh giảm `maxCapacity` xuống thấp hơn số slot đã được giữ.
- Tránh admin sau ghi đè thay đổi của admin trước.

## 7. Transaction Cấp Phát Đơn Thuốc (Prescription Dispensing)

### Flow liên quan

- `PrescriptionServiceImpl.dispensePrescription(...)`

### Khi nào cần transaction

Cần transaction khi Dược sĩ thực hiện cấp phát đơn thuốc. Quy trình này kiểm tra tồn kho của từng thuốc trong đơn, thực hiện trừ kho theo nguyên tắc FIFO (lô hết hạn trước trừ trước) trên nhiều lô thuốc hoạt động, và chuyển trạng thái đơn thuốc sang đã phát. Toàn bộ các thao tác này phải thực thi nguyên tử (atomic) và an toàn trước tranh chấp đồng thời từ các dược sĩ cấp phát khác hoặc từ bác sĩ đang kê đơn/sửa đơn.

### Hành động trong transaction

- Lock `Prescription` bằng `PrescriptionRepository.findByPrescriptionIdAndIsActive(...)`.
- Duyệt qua từng thuốc trong đơn thuốc (`PrescriptionDetail`):
  - Lock toàn bộ các lô thuốc (`MedicineLot`) còn hoạt động của thuốc đó bằng khóa bi quan `PESSIMISTIC_WRITE` qua phương thức `MedicineLotRepository.findAllByMedicineIdAndIsActiveForUpdate(...)`. Điều này ngăn chặn bất kỳ tiến trình kê đơn hay cấp phát nào khác đọc hoặc sửa đổi các lô thuốc này cùng lúc.
  - Lọc bỏ các lô thuốc đã hết hạn sử dụng và tính toán tổng tồn kho thực tế khả dụng real-time.
  - Kiểm tra nếu tổng tồn kho khả dụng nhỏ hơn yêu cầu kê đơn, ném ra lỗi `BusinessException` (giao dịch tự động rollback hoàn toàn).
  - Trừ kho theo thuật toán **FIFO**: Sắp xếp các lô khả dụng theo ngày hết hạn `expiryDate` tăng dần, trừ dần số lượng từ lô hết hạn trước đến lô hết hạn sau.
  - Gọi `MedicineLotRepository.save(...)` cập nhật số lượng tồn kho mới của lô thuốc.
- Đổi trạng thái đơn thuốc sang đã phát bằng cách set `isActive = 0`.
- Lưu đơn thuốc bằng `PrescriptionRepository.save(...)`.

### Lý lý cần transaction

- Tránh tình trạng hai dược sĩ cùng cấp phát hai đơn thuốc khác nhau có chung loại thuốc và trừ trùng số lượng lô (lost update), dẫn đến tồn kho âm hoặc sai lệch số liệu.
- Tránh tình trạng một bác sĩ đang kê đơn thuốc đó cùng lúc làm sụt giảm kho dược khả dụng mà không có khóa kiểm soát, gây ra write skew.
- Đảm bảo tính nguyên tử: nếu có bất kỳ một thuốc nào trong đơn không đủ tồn kho, toàn bộ giao dịch trừ kho của các thuốc trước đó sẽ rollback hoàn chỉnh, không gây rác dữ liệu tồn kho.

## 8. Transaction Ngừng Kinh Doanh Thuốc (Medicine SKU Deactivation)

### Flow liên quan

- `MedicineServiceImpl.deactivateMedicine(...)`

### Khi nào cần transaction

Cần transaction khi Dược sĩ hoặc Admin thực hiện ngừng kinh doanh (deactivate) một loại thuốc (Medicine SKU). Ràng buộc nghiệp vụ quy định chỉ cho phép ngừng kinh doanh nếu tổng tồn kho của toàn bộ các lô thuốc hoạt động còn lại của thuốc đó nhỏ hơn hoặc bằng 10 (`totalStock <= 10`).

### Hành động trong transaction

- Đọc thông tin loại thuốc và kiểm tra tính hợp lệ qua `findActiveMedicineById(id)`.
- Lock toàn bộ các lô thuốc đang hoạt động của loại thuốc đó bằng khóa bi quan `PESSIMISTIC_WRITE` qua `MedicineLotRepository.findAllByMedicineIdAndIsActiveForUpdate(id, 1)`.
- Tính tổng số lượng tồn kho của toàn bộ các lô đã khóa.
- Nếu tổng số lượng lớn hơn 10, ném ra lỗi `BusinessException` (giao dịch tự động rollback).
- Nếu hợp lệ (tổng tồn kho <= 10), tiến hành cập nhật trạng thái thuốc `isActive = 0` (ngừng kinh doanh) và lưu lại qua `MedicineRepository.save(...)`.

### Lý do cần transaction

- Tránh tình trạng tranh chấp đồng thời (race condition): một người thực hiện ngừng kinh doanh thuốc này trong khi người khác đồng thời nhập thêm lô mới cho thuốc đó hoặc cập nhật tăng số lượng lô hiện tại.
- Nếu không lock bằng khóa bi quan, flow ngừng kinh doanh có thể đọc thấy tổng tồn kho cũ <= 10 và cho phép deactive thành công, trong khi thực tế số lượng tồn kho vừa được tăng lên vượt quá 10 (vi phạm ràng buộc toàn vẹn).
- Đảm bảo tính nhất quán dữ liệu giữa bảng Medicine và MedicineLot trong quá trình cập nhật trạng thái.

## Tóm Tắt

Project có 8 nhóm transaction chính:

- Đặt lịch online.
- Đặt lịch walk-in đã thanh toán.
- Đổi trạng thái appointment.
- Release slot.
- Thanh toán.
- Medical record, request workflow và doctor schedule.
- Cấp phát đơn thuốc và kiểm soát kho dược FIFO.
- Ngừng kinh doanh thuốc và kiểm soát tồn kho tối đa.


