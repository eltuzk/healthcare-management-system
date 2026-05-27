# Các Chỗ Đang Xử Lý Concurrency Trong Project

Tài liệu này chỉ ra các điểm trong project hiện đang có xử lý concurrency. Mỗi mục gồm hai phần: vấn đề và cách giải quyết.

---

## 1. Đặt Lịch Khám Online

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Lost Update** | Nhiều bệnh nhân cùng lúc đặt ca khám `DoctorSchedule`. Hai request song song cùng đọc thấy `current_booking_count` là `X` $\rightarrow$ cùng tăng số lượng lên `X + 1` và ghi đè chồng lên nhau, làm mất số lượng đếm đặt lịch thực tế. |
| **Write Skew** | Hai request cùng đọc thấy `current_booking_count = 9` (giới hạn `max_capacity = 10`), cả hai đều thấy còn 1 chỗ trống và thỏa mãn điều kiện. Sau đó cả hai cùng chèn cuộc hẹn mới $\rightarrow$ tổng số lượng đặt sau commit là 11, vượt quá giới hạn thiết kế của ca trực bác sĩ. |
| **Phantom Read** | Luồng kiểm tra lịch khám hoạt động của bệnh nhân (`active appointment`) chạy song song với luồng tạo lịch khám mới. Luồng kiểm tra không thấy dòng nào hợp lệ (do dòng mới chưa được commit), dẫn đến quyết định cho phép tạo lịch khám mới. |
| **Duplicate Insert Race** | Hai request cùng đặt lịch khám cho 1 bệnh nhân chạy cực kỳ song song $\rightarrow$ cùng kiểm tra thấy chưa có lịch khám hoạt động $\rightarrow$ cả hai cùng chèn thành công bản ghi mới, dẫn đến bệnh nhân có 2 ca khám hoạt động trùng lặp. |

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.create(...)` bằng `@Transactional`. Khi tạo lịch, service lock bệnh nhân bằng `PatientRepository.findByIdForUpdate(...)` và lock ca khám bằng `DoctorScheduleRepository.findByIdForUpdate(...)` với `PESSIMISTIC_WRITE`. Sau đó mới kiểm tra sức chứa, tăng `currentBookingCount`, tăng `lastQueueNumber`, cấp `queue_num`, tạo appointment và tạo `PaymentRecord` trong cùng một transaction.

Database có unique constraint `(doctor_schedule_id, queue_num)` để chặn trùng số thứ tự ở tầng DB.

---

## 2. Đặt Lịch Walk-in Đã Thanh Toán

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Lost Update** | Nhiều nhân viên tiếp đón đặt lịch walk-in cho cùng ca trực. Hai request song song đọc `current_booking_count` cũ và ghi đè counter của nhau, làm mất bản ghi đếm thực tế. |
| **Write Skew** | Hai request cùng thấy ca trực còn đúng 1 slot trống $\rightarrow$ cả hai cùng chèn lịch khám thành công $\rightarrow$ tổng số lượng đặt vượt quá sức chứa tối đa của ca trực. |
| **Inconsistent Write** | Khi thanh toán và tạo lịch trực tiếp, hệ thống cần ghi đồng thời vào `Appointment`, `PaymentRecord`, và `PaymentTransaction`. Nếu không có transaction atomic, một phần ghi thành công (ví dụ tạo lịch) nhưng phần khác lỗi (không ghi được giao dịch tiền mặt) $\rightarrow$ dữ liệu bị lệch trạng thái nghiêm trọng. |

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.createWalkInPaidAppointment(...)` bằng `@Transactional`. Flow này lock `Patient` và `DoctorSchedule` trước khi cấp slot. Appointment được tạo ở trạng thái `CHECKED_IN`, đồng thời tạo `PaymentRecord` và `PaymentTransaction` gateway `CASH` trong cùng transaction. Nếu một bước lỗi, toàn bộ flow rollback.

---

## 3. Cấp Và Release Slot Của Ca Khám

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Lost Update** / **Double Decrement** | Khi hai luồng cùng lúc xử lý hủy lịch khám hoặc hết hạn thanh toán cho cùng một ca trực `DoctorSchedule`. Chúng cùng đọc `current_booking_count` cũ và cùng giảm đi 1 $\rightarrow$ đè counter của nhau hoặc làm giảm sai lệch kép giá trị thực tế của counter. |
| **Inconsistent Write** | Lịch hẹn khám cập nhật trạng thái thành `CANCELLED` hoặc `PAYMENT_EXPIRED` thành công nhưng bước giảm slot `current_booking_count` của bác sĩ bị lỗi hoặc ngược lại, dẫn đến số lượng thống kê slot trống bị lệch hoàn toàn với thực tế danh sách cuộc hẹn. |

### Cách giải quyết

Project chỉ release slot trong transaction. Khi release, service gọi `findDoctorScheduleForUpdateOrThrow(...)` để lock `DoctorSchedule` rồi mới giảm `currentBookingCount`. `lastQueueNumber` không bị giảm để tránh cấp lại số thứ tự cũ.

Các flow liên quan nằm trong `AppointmentServiceImpl.cancel(...)` và `AppointmentServiceImpl.expirePendingPaymentReservations(...)`.

---

## 5. Check-in Appointment

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Lost Update** | Hai nhân viên tiếp đón cùng mở một lịch hẹn khám và bấm check-in gần như đồng thời. Một request đọc trạng thái cũ `CONFIRMED` rồi cập nhật lên `CHECKED_IN`, trong khi request kia cũng làm tương tự và ghi đè trạng thái của nhau. |
| **Non-repeatable Read** / **Race Condition** | Nhân viên đang tiến hành check-in lịch hẹn (đọc thấy trạng thái đang là `CONFIRMED`), nhưng cùng lúc luồng khác vừa đổi trạng thái lịch hẹn thành `CANCELLED` $\rightarrow$ luồng check-in tiếp tục ghi đè trạng thái cũ lên trạng thái mới hơn đã thay đổi. |

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.checkIn(...)`. Service lock appointment bằng `findByIdForUpdate(...)`, sau đó chỉ cho chuyển trạng thái từ `CONFIRMED` sang `CHECKED_IN`. Vì appointment bị lock trong transaction, chỉ một request được đổi trạng thái tại một thời điểm.

---

## 6. Start Appointment

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Lost Update** | Hai bác sĩ hoặc hai tab của một bác sĩ cùng gửi yêu cầu bắt đầu khám (`start`) cho một cuộc hẹn. Cả hai cùng đọc thấy trạng thái cũ `CHECKED_IN` và cùng cập nhật trạng thái lên `IN_PROGRESS`, ghi đè thông tin phiên làm việc của nhau. |
| **Non-repeatable Read** / **Race Condition** | Bác sĩ bấm bắt đầu khám (đọc thấy trạng thái `CHECKED_IN`), nhưng cùng lúc luồng khác vừa cập nhật trạng thái cuộc hẹn thành `CANCELLED` do hết hạn thanh toán $\rightarrow$ bác sĩ vẫn ghi đè thành công trạng thái `IN_PROGRESS` dựa trên dữ liệu cũ không còn chính xác. |

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.start(...)`. Service lock appointment bằng `findByIdForUpdate(...)`, sau đó chỉ cho chuyển trạng thái từ `CHECKED_IN` sang `IN_PROGRESS`. Các request song song phải chờ lock và sẽ thấy trạng thái mới nhất sau khi transaction trước commit.

---

## 7. Xác Nhận Thanh Toán Qua SePay Webhook

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Duplicate Processing** / **Idempotency Race** | Cùng một giao dịch thanh toán từ SePay được gửi lặp lại (webhook retry) hoặc gửi song song. Nếu không kiểm soát trùng, hệ thống sẽ chèn nhiều bản ghi giao dịch `PaymentTransaction` trùng lặp cho cùng một mã lịch hẹn. |
| **Lost Update** / **Non-repeatable Read** | Hai webhook báo thanh toán cho cùng một cuộc hẹn chạy song song. Cả hai cùng kiểm tra thấy lịch hẹn đang ở trạng thái `PENDING` $\rightarrow$ cả hai cùng tiến hành ghi nhận thanh toán và cập nhật `payment_status = PAID`, dẫn đến ghi đè công nợ chồng chéo và lệch số tiền đã nhận. |

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.confirmPaymentFromSepayWebhook(...)` bằng `@Transactional`. Service kiểm tra idempotency qua `PaymentTransactionRepository.findBySepayTransactionId(...)`. Sau đó lock appointment bằng `AppointmentRepository.findByAppointmentCodeForUpdate(...)`, lock payment record bằng `PaymentRecordRepository.findByAppointmentIdForUpdate(...)`, rồi cập nhật appointment, payment record và tạo payment transaction trong cùng transaction.

Database cũng có unique constraint cho `Appointment.sepayTransactionId` và `PaymentTransaction.sepayTransactionId`. Khi save, project dùng `saveAndFlush(...)` và bắt `DataIntegrityViolationException` để phát hiện conflict sớm.

---

## 8. Scheduler Hết Hạn Thanh Toán

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Race Condition** / **Lost Update** | Scheduler chạy quét các lịch hẹn quá 10 phút để hủy và giải phóng slot, cùng lúc đó Webhook thanh toán SePay đang xác nhận giao dịch thành công. Nếu không khóa dữ liệu, luồng Scheduler đọc thấy lịch hẹn là `PENDING` $\rightarrow$ cập nhật thành `PAYMENT_EXPIRED` và giải phóng slot; cùng lúc luồng Webhook cũng thấy `PENDING` $\rightarrow$ cập nhật thành `CONFIRMED` $\rightarrow$ kết quả là lịch hẹn vừa bị hủy vừa được xác nhận, slot bị giảm sai lệch. |
| **Non-repeatable Read** | Scheduler đọc dữ liệu lịch hẹn thấy `PENDING` để chuẩn bị hủy, nhưng ngay lập tức Webhook vừa ghi nhận thanh toán và đổi sang `CONFIRMED`. Scheduler không đọc lại mà ghi đè trạng thái hủy cũ lên trạng thái đã thanh toán mới. |

### Cách giải quyết

Project xử lý trong `AppointmentServiceImpl.expirePendingPaymentReservations(...)`. Scheduler lấy danh sách appointment hết hạn bằng `AppointmentRepository.findExpiredPaymentReservationsForUpdate(...)`, query này dùng `PESSIMISTIC_WRITE`. Sau khi lock appointment, service release slot và chuyển appointment sang `PAYMENT_EXPIRED` trong cùng transaction.

---

## 9. Tạo Medical Record Từ Appointment

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Phantom Read** | Hai request cùng gửi yêu cầu tạo bệnh án cho một cuộc hẹn. Cả hai transaction chạy song song và cùng thực hiện câu lệnh kiểm tra `existsByAppointmentId` $\rightarrow$ đều không thấy dòng bệnh án nào tồn tại (do transaction kia chưa commit) $\rightarrow$ cả hai cùng chèn thành công bản ghi mới. |
| **Duplicate Insert Race** | Dẫn đến cuộc hẹn khám có hai hồ sơ bệnh án y khoa hoạt động trùng lặp chèn cùng lúc, vi phạm quy tắc 1 lịch hẹn chỉ có tối đa 1 bệnh án. |

### Cách giải quyết

Project xử lý trong `MedicalRecordServiceImpl.createFromAppointment(...)` bằng `@Transactional`. Service kiểm tra appointment phải đang `IN_PROGRESS`, kiểm tra chưa có medical record, sau đó save bằng `saveAndFlush(...)`. Database có unique constraint trên `MEDICAL_RECORD.appointment_id`; nếu hai request cùng pass validate, constraint DB vẫn chặn và service bắt `DataIntegrityViolationException` để trả lỗi duplicate.

---

## 10. Cập Nhật Medical Record

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Lost Update** | Bác sĩ mở bệnh án ở hai trình duyệt hoặc hai bác sĩ cùng thảo luận và sửa bệnh án song song. Bác sĩ B bấm lưu trước (DB lưu dữ liệu của B), sau đó Bác sĩ A bấm lưu sau (DB ghi đè toàn bộ dữ liệu của A lên trên bản ghi của B), làm biến mất hoàn toàn các thông tin chẩn đoán lâm sàng quý giá mà Bác sĩ B vừa cập nhật. |

### Cách giải quyết

Project dùng optimistic locking cho `MedicalRecord` qua field `versionNumber` với `@Version`. Trong `MedicalRecordServiceImpl.update(...)`, client phải gửi `version`. Service so sánh version request với version hiện tại; nếu lệch thì throw `ObjectOptimisticLockingFailureException`. `GlobalExceptionHandler` trả lỗi này thành HTTP `409 CONFLICT`.

---

## 11. Complete Medical Record Và Complete Appointment

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Inconsistent Write** | Khi bác sĩ hoàn tất bệnh án, hệ thống phải cập nhật `MedicalRecord.status = COMPLETED` và đồng thời cập nhật `Appointment.status = COMPLETED`. Nếu không có transaction atomic bảo vệ, một phần ghi thành công (ví dụ hoàn thành bệnh án) nhưng phần kia lỗi (lịch hẹn vẫn ở trạng thái `IN_PROGRESS`), làm lệch vòng đời hồ sơ bệnh nhân. |
| **Lost Update** | Trùng khớp thời điểm hoàn tất, có luồng khác đang sửa lịch hẹn hoặc bệnh án song song $\rightarrow$ nếu không khóa dữ liệu sẽ ghi đè và làm mất trạng thái cập nhật mới của nhau. |

### Cách giải quyết

Project xử lý trong `MedicalRecordServiceImpl.complete(...)` và `MedicalRecordWorkflowServiceImpl.completeIfReady(...)` bằng `@Transactional`. Service lock `MedicalRecord` bằng repository method có `PESSIMISTIC_WRITE`, kiểm tra MR đang `IN_PROGRESS`, kiểm tra các trường clinical bắt buộc và kiểm tra toàn bộ request liên quan đã `RESULT_AVAILABLE`. Sau đó service cập nhật `MedicalRecord.status = COMPLETED`, set `completedAt`, đồng thời set `appointment.status = COMPLETED` trong cùng transaction.

---

## 12. Tạo Request Xét Nghiệm/Dịch Vụ Và Sync Billing

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Lost Update** / **Stale Read** | Bác sĩ chỉ định thêm xét nghiệm cận lâm sàng mới (tăng tổng tiền điều trị), cùng lúc đó luồng kế toán đang đọc `total_price` cũ để ghi nhận thanh toán. Nếu không khóa dữ liệu, kế toán sẽ thu tiền dựa trên số liệu cũ dở dang (đọc stale), sau đó bản ghi tổng tiền mới ghi đè lên nhưng số tiền thực tế thu được bị thiếu hụt. |
| **Inconsistent Write** | Việc tạo xét nghiệm yêu cầu ghi đồng thời vào bảng `LabTestRequest`, `LabTestRequestItem`, cập nhật tổng tiền `total_price` ở `MedicalRecord` và cập nhật `total_price` ở `PaymentRecord`. Nếu lỗi ở một bước và không rollback, thông tin chỉ định lâm sàng và hóa đơn tài chính sẽ bị lệch nhau hoàn toàn. |

### Cách giải quyết

Project xử lý trong `LabTestRequestServiceImpl.createRequest(...)` và `MedicalServiceRequestServiceImpl.createRequest(...)` bằng `@Transactional`. Service lock `MedicalRecord` qua `MedicalRecordRepository.findByIdForUpdate(...)`, chỉ cho tạo request khi MR đang `DRAFT`, lưu request/items, rồi gọi `MedicalRecordBillingService.syncBilling(...)` để tính lại tổng lab/service và sync sang payment record trong cùng transaction.

---

## 13. Thanh Toán Medical Record

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Stale Read** / **Lost Update** | Hai nhân viên thu ngân cùng thực hiện thanh toán tiền mặt cho cùng một bệnh án song song. Cả hai cùng đọc thấy trạng thái hóa đơn là `UNPAID` $\rightarrow$ cùng thu tiền và cùng ghi nhận thanh toán, dẫn đến ghi đè công nợ chồng chéo và thất thoát quỹ. |
| **Double Payment** | Chèn 2 bản ghi giao dịch thanh toán thành công trùng lặp cho cùng một mã bệnh án. |
| **Inconsistent Write** | Trạng thái thanh toán của hóa đơn chuyển sang `PAID` nhưng bệnh án y khoa vẫn bị kẹt ở trạng thái nháp `DRAFT` không thể chuyển sang `IN_PROGRESS` để tiếp tục khám, làm nghẽn toàn bộ luồng nghiệp vụ. |

### Cách giải quyết

Project xử lý trong `PaymentRecordServiceImpl.recordMedicalRecordCashPayment(...)` bằng `@Transactional`. Service lock `MedicalRecord`, sync billing lại ngay trước khi thu tiền, lock `PaymentRecord` bằng `findByMedicalRecordIdForUpdate(...)`, kiểm tra số tiền thu khớp chính xác tổng tiền, tạo `PaymentTransaction` gateway `CASH`, set payment `PAID`, rồi chuyển MR sang `IN_PROGRESS` trong cùng transaction.

---

## 14. Cập Nhật Request Và Tạo Result

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Lost Update** / **Duplicate Result** | Hai kỹ thuật viên phòng xét nghiệm cùng nhập kết quả cho một yêu cầu xét nghiệm song song $\rightarrow$ cùng đọc thấy trạng thái cũ `SAMPLE_COLLECTED` $\rightarrow$ cùng chèn 2 bản ghi kết quả xét nghiệm trùng lặp, gây ra sai lệch hồ sơ chẩn đoán cận lâm sàng. |
| **Invalid State Transition** | Cập nhật trạng thái xét nghiệm sai thứ tự (ví dụ tạo kết quả xét nghiệm khi bệnh nhân chưa thanh toán hoặc chưa lấy mẫu thử). |
| **Non-repeatable Read** | Luồng cập nhật đọc thấy bệnh án đã được thanh toán, nhưng ngay lập tức luồng khác rollback thanh toán $\rightarrow$ nếu không khóa dữ liệu sẽ ghi nhận kết quả bất hợp pháp dựa trên trạng thái cũ. |

### Cách giải quyết

Project xử lý trong `LabTestRequestServiceImpl` và `MedicalServiceRequestServiceImpl` bằng transaction. Khi cập nhật status hoặc tạo result, service lock request bằng `findByIdForUpdate(...)`, validate MR liên quan đang `IN_PROGRESS` và payment record của MR đã `PAID`. Cập nhật trạng thái thủ công chỉ cho phép sang `SAMPLE_COLLECTED`; tạo result yêu cầu request đang `SAMPLE_COLLECTED` và sau đó set request `RESULT_AVAILABLE`.

Database cũng có quan hệ 1-1 giữa request và result (`LAB_TEST_RESULT.lab_test_request_id` unique, `MEDICAL_SERVICE_RESULT.med_ser_req_id` unique) để chặn result trùng ở tầng DB.

---

## 15. Auto-complete Medical Record Sau Khi Request Hoàn Thành

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Race Condition** / **Lost Update** | Hai kỹ thuật viên cận lâm sàng cùng hoàn thành 2 xét nghiệm cuối cùng của bệnh án gần như đồng thời $\rightarrow$ cả hai luồng xử lý song song cùng thực hiện kiểm tra điều kiện "tất cả dịch vụ đã có kết quả" $\rightarrow$ cả hai cùng thấy thỏa mãn và cùng kích hoạt logic hoàn tất bệnh án & lịch hẹn, dẫn đến ghi đè chồng chéo trạng thái và thời gian hoàn tất. |
| **Inconsistent Write** | Trạng thái bệnh án cập nhật thành `COMPLETED` nhưng cập nhật trạng thái lịch hẹn liên quan bị lỗi hoặc bị nghẽn, làm lệch pha trạng thái nghiệp vụ. |

### Cách giải quyết

Sau khi tạo result, service gọi `MedicalRecordWorkflowService.completeIfReady(...)`. Workflow service lock `MedicalRecord`, kiểm tra MR vẫn `IN_PROGRESS`, kiểm tra các trường clinical bắt buộc, kiểm tra toàn bộ lab/service request đều `RESULT_AVAILABLE`, rồi set MR và appointment sang `COMPLETED` trong cùng transaction.

---

## 16. Báo Cáo Doanh Thu

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Dirty Read** / **Inconsistent Read** | Luồng chạy báo cáo doanh thu kế toán thực hiện quét dữ liệu ngay lúc một transaction thanh toán viện phí trị giá lớn đang diễn ra dở dang (chưa commit). Báo cáo đọc phải số liệu chưa chính thức này $\rightarrow$ sau đó transaction thanh toán bị lỗi và rollback $\rightarrow$ báo cáo kế toán bị sai lệch số liệu thực tế so với dòng tiền thực tế. |

### Cách giải quyết

Project xử lý trong `RevenueReportServiceImpl.getRevenueReport(...)` bằng read-only transaction. Báo cáo chỉ đọc `PAYMENT_TRANSACTION` có `process_status = SUCCESS`, join sang `PAYMENT_RECORD` để suy ra owner type `APPOINTMENT` hoặc `MEDICAL_RECORD`, và không tự ghi dữ liệu kế toán. Flow ghi payment vẫn chịu trách nhiệm transaction/lock trước khi commit, nên báo cáo chỉ nhìn thấy dữ liệu đã ổn định sau commit.

---

## 17. Tạo Lịch Làm Việc Bác Sĩ

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Phantom Read** / **Duplicate Insert Race** | Hai người quản trị (Admin) cùng lúc gán lịch làm việc cho Bác sĩ A vào cùng một ngày và cùng một ca trực (Sáng/Chiều). Cả hai transaction chạy cực kỳ song song $\rightarrow$ cùng truy vấn `existsBy...` không thấy lịch nào trùng $\rightarrow$ cả hai cùng chèn thành công bản ghi mới, dẫn đến Bác sĩ A bị xếp 2 lịch trực trùng nhau. |

### Cách giải quyết

Project có unique constraint ở database cho `(doctor_id, schedule_date, shift)` và `(room_id, schedule_date, shift)`. Trong `DoctorScheduleServiceImpl.create(...)`, service vẫn validate trước, nhưng khi save dùng `saveAndFlush(...)` và bắt `DataIntegrityViolationException` trong `saveWithDuplicateHandling(...)` để trả lỗi nghiệp vụ rõ ràng nếu có race condition.

---

## 18. Cập Nhật Lịch Làm Việc Bác Sĩ

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Write Skew** | Người quản trị đang thực hiện điều chỉnh giảm sức chứa tối đa (`maxCapacity`) của phòng khám từ 20 xuống 10, cùng lúc đó luồng bệnh nhân đang đặt chỗ song song làm tăng số lượng đặt (`currentBookingCount`) lên 15. Nếu không khóa, admin đọc thấy `currentBookingCount` cũ là 8 và cho phép giảm sức chứa xuống 10, dẫn đến kết quả sau commit số lượng đặt thực tế (15) vượt quá sức chứa tối đa thiết kế mới (10). |
| **Lost Update** | Hai admin cùng lúc sửa thông tin của một ca trực và ghi đè chồng chéo lên nhau làm biến mất thay đổi của nhau. |

### Cách giải quyết

Project kiểm tra trong `DoctorScheduleServiceImpl.update(...)`: `maxCapacity` mới không được nhỏ hơn `currentBookingCount`. Entity `DoctorSchedule` cũng có `@Version` qua `versionNumber`, giúp phát hiện update ghi đè khi nhiều transaction cùng sửa cùng một lịch.

---

## 19. Optimistic Locking Cho Appointment Và DoctorSchedule

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Lost Update** | Xảy ra khi một bản ghi ca trực hoặc lịch hẹn bị nhiều luồng tác vụ độc lập cập nhật song song ngoài các luồng đã được bảo vệ bằng Khóa bi quan. Nếu không có cơ chế phát hiện sửa đổi đồng thời, giao dịch commit sau cùng sẽ ghi đè đè lên toàn bộ thay đổi của giao dịch trước mà không hề hay biết. |

### Cách giải quyết

Project thêm `versionNumber` và `@Version` cho `Appointment` và `DoctorSchedule`. Migration `V6__add_version_columns_for_optimistic_locking.sql` thêm cột `version_number` cho hai bảng này. Khi Hibernate phát hiện version đã thay đổi, nó sẽ throw optimistic locking exception thay vì ghi đè dữ liệu.

---

## 20. Unique Constraint Cho Các Dữ Liệu Dễ Bị Tạo Trùng

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Phantom Read** / **Duplicate Insert Race** | Hai bệnh nhân mới đăng ký tài khoản cùng lúc với chung một email, hoặc hai nhân viên tạo mã lịch hẹn khám chạy song song. Cả hai luồng cùng đọc thấy mã/email chưa tồn tại $\rightarrow$ cả hai cùng insert thành công, tạo ra dữ liệu trùng lặp nghiêm trọng trong hệ thống. |

### Cách giải quyết

Project đặt unique constraint ở database cho các field quan trọng. Service có thể check trước bằng `existsBy...`, nhưng tầng bảo vệ cuối cùng vẫn là database constraint. Một số service bắt `DataIntegrityViolationException` để đổi lỗi DB thành lỗi nghiệp vụ dễ hiểu hơn.

---

## 21. Cấp Phát Đơn Thuốc Và Trừ Kho

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Lost Update** | Hai dược sĩ cùng lúc phát thuốc cho hai đơn thuốc chứa chung loại thuốc. Cả hai luồng cùng đọc số lượng tồn của lô thuốc hiện tại (ví dụ 100 viên) và cùng thực hiện trừ kho (luồng 1 trừ 10 viên, luồng 2 trừ 20 viên), sau đó cùng ghi đè giá trị lên DB $\rightarrow$ kết quả tồn kho sau commit là 80 hoặc 90 thay vì 70 viên thực tế. |
| **Write Skew** | Bác sĩ đang kiểm tra kho thấy đủ thuốc nên kê đơn thành công, nhưng cùng mili-giây đó dược sĩ phát thuốc vừa trừ hết số lượng thuốc khả dụng, khiến đơn thuốc vừa kê không có thuốc thực tế để cấp phát. |
| **Inconsistent Write** | Khi cấp phát đơn thuốc, một phần thuốc trong đơn được trừ kho thành công nhưng loại thuốc sau bị thiếu và giao dịch không được rollback đồng bộ $\rightarrow$ dẫn đến trạng thái kho bị lệch và đơn thuốc bị kẹt dở dang. |

### Cách giải quyết

Project xử lý trong `PrescriptionServiceImpl.dispensePrescription(...)` bằng `@Transactional`.

Khi bắt đầu cấp phát, với mỗi loại thuốc có trong đơn, service sẽ:
1. Gọi `MedicineLotRepository.findAllByMedicineIdAndIsActiveForUpdate(medicineId, 1)` sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` để khóa ghi tất cả các lô thuốc đang hoạt động của thuốc đó tại tầng database. Việc này ngăn chặn các transaction kê đơn (trong `validatePrescriptionDetails`) hoặc cấp phát khác truy cập đồng thời vào các lô thuốc của dược phẩm này.
2. Kiểm tra tổng tồn kho khả dụng real-time (bằng cách loại bỏ các lô đã hết hạn). Nếu không đủ số lượng, lập tức throw `BusinessException` để rollback toàn bộ giao dịch.
3. Áp dụng thuật toán FIFO: sắp xếp các lô khả dụng theo ngày hết hạn `expiryDate` tăng dần và tiến hành trừ kho an toàn.
4. Cập nhật trạng thái đơn thuốc sang đã cấp phát (`isActive = 0`).

Tương tự, tại luồng kê đơn (`PrescriptionServiceImpl.createPrescription`), service cũng gọi `validatePrescriptionDetails` có sử dụng Pessimistic Lock tương tự để kiểm tra tồn kho khả dụng trước khi cho phép bác sĩ kê đơn thành công, loại bỏ hoàn toàn khả năng kê đơn vượt quá tồn kho thực tế.

---

## 22. Ngừng Kinh Doanh Thuốc (Medicine SKU Deactivation)

### Vấn đề

| Loại vấn đề | Tại sao xảy ra (Kịch bản chi tiết nếu không có kiểm soát) |
| :--- | :--- |
| **Write Skew** / **Lost Update** | Admin thực hiện ngừng bán loại thuốc X (chỉ được làm khi tổng tồn kho của các lô `<= 10`). Hệ thống đọc thấy tổng tồn kho cũ là 8 (thỏa mãn) $\rightarrow$ cho phép cập nhật deactive. Nhưng cùng lúc đó, thủ kho đang thực hiện nhập lô mới 100 viên thành công. Giao dịch ngừng bán commit sau đó ghi đè trạng thái `isActive = 0` $\rightarrow$ kết quả là một loại thuốc vẫn còn 108 viên thực tế lại bị khóa không thể bán được, vi phạm nghiêm trọng quy tắc nghiệp vụ của nhà thuốc. |

### Cách giải quyết

Project giải quyết triệt để trong `MedicineServiceImpl.deactivateMedicine(...)` bằng `@Transactional` kết hợp Khóa Bi quan (Pessimistic Lock).

Khi bắt đầu deactive thuốc:
1. Service thực hiện lock ghi toàn bộ các lô thuốc đang hoạt động của thuốc đó tại tầng database bằng cách gọi `MedicineLotRepository.findAllByMedicineIdAndIsActiveForUpdate(id, 1)`. Phương thức này sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` khóa tất cả các dòng dữ liệu liên quan trong bảng `MedicineLot`.
2. Bất kỳ giao dịch nhập lô mới hoặc chỉnh sửa số lượng lô của thuốc này sẽ bị chặn và phải chờ cho đến khi transaction của Admin hoàn thành.
3. Tính toán tổng tồn kho thực tế của các lô thuốc đã khóa an toàn. Nếu tổng tồn kho vượt quá 10, lập tức ném ra lỗi `BusinessException` để rollback toàn bộ giao dịch, đảm bảo không có trạng thái không nhất quán.
4. Nếu tổng tồn kho hợp lệ (<= 10), cập nhật `isActive = 0` trên entity `Medicine` và lưu lại thông qua `MedicineRepository.save(...)`.
