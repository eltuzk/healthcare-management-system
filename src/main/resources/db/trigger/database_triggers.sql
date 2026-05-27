-- ==========================================================================================
-- NHÓM 1: CÁC DATABASE TRIGGERS HIỆN CÓ CỦA HỆ THỐNG (DATABASE-LEVEL TRIGGERS)
-- Tệp tin này tổng hợp toàn bộ 7 trigger đang được định nghĩa và triển khai ở mức Cơ sở dữ liệu.
-- Mỗi trigger đều có đánh số thứ tự và phần mô tả chi tiết bằng Tiếng Việt có dấu.
-- Cú pháp: Oracle PL/SQL đồng bộ với cấu hình CSDL của hệ thống.
-- ==========================================================================================

-- ------------------------------------------------------------------------------------------
-- TRIGGER 1: trg_prevent_cancel_paid_appt
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: APPOINTMENT (Lịch hẹn khám)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: BEFORE UPDATE OR DELETE (Trước khi cập nhật hoặc xóa bộ dữ liệu)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Trigger này đóng vai trò bảo vệ tính toàn vẹn của lịch hẹn khám bệnh:
--     1. Khi xóa (DELETING): Ngăn cản xóa lịch hẹn nếu lịch hẹn đã có thông tin thanh toán (paid_at IS NOT NULL)
--        hoặc trạng thái cuộc hẹn thuộc nhóm đã được xử lý (CONFIRMED, CHECKED_IN, IN_PROGRESS, COMPLETED).
--        Kích hoạt lỗi -20001 nếu cố tình thực hiện.
--     2. Khi cập nhật (UPDATING): Ngăn cản việc thay đổi trạng thái cuộc hẹn thành 'CANCELLED' (Đã hủy) nếu
--        trước đó lịch hẹn đó đã được thanh toán hoặc đã xác nhận/đang diễn ra/đã hoàn tất.
--        Kích hoạt lỗi -20002 nếu cố tình thực hiện.
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_prevent_cancel_paid_appt
BEFORE UPDATE OR DELETE ON APPOINTMENT
FOR EACH ROW
BEGIN
    IF DELETING THEN
        IF :OLD.status IN ('CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED') OR :OLD.paid_at IS NOT NULL THEN
            RAISE_APPLICATION_ERROR(-20001, 'Không thể xóa lịch khám đã thanh toán hoặc đã xác nhận.');
        END IF;
    ELSIF UPDATING THEN
        IF :NEW.status = 'CANCELLED' AND (:OLD.status IN ('CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED') OR :OLD.paid_at IS NOT NULL) THEN
            RAISE_APPLICATION_ERROR(-20002, 'Không thể hủy (CANCEL) lịch khám đã thanh toán hoặc đang tiến hành.');
        END IF;
    END IF;
END;
/


-- ------------------------------------------------------------------------------------------
-- TRIGGER 2: trg_prevent_locked_mr_update
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: MEDICAL_RECORD (Hồ sơ bệnh án)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: BEFORE UPDATE OR DELETE (Trước khi cập nhật hoặc xóa bộ dữ liệu)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Trigger này bảo vệ tuyệt đối hồ sơ bệnh án y tế một khi đã được bác sĩ chốt và khóa (LOCKED):
--     - Nếu trạng thái cũ của bệnh án đang là 'LOCKED':
--       1. Khi xóa (DELETING): Ngăn chặn hoàn toàn việc xóa hồ sơ bệnh án (Kích hoạt lỗi -20003).
--       2. Khi cập nhật (UPDATING): Ngăn chặn việc sửa đổi thông tin bệnh án (Kích hoạt lỗi -20004), 
--          ngoại trừ trường hợp chuyển đổi trạng thái bệnh án ra khỏi trạng thái LOCKED thông qua quy trình hợp lệ.
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_prevent_locked_mr_update
BEFORE UPDATE OR DELETE ON MEDICAL_RECORD
FOR EACH ROW
BEGIN
    IF :OLD.status = 'LOCKED' THEN
        IF DELETING THEN
            RAISE_APPLICATION_ERROR(-20003, 'Không thể xóa bệnh án đã khóa (LOCKED).');
        ELSIF UPDATING THEN
            IF :NEW.status = 'LOCKED' THEN 
                RAISE_APPLICATION_ERROR(-20004, 'Không thể chỉnh sửa bệnh án đã bị khóa (LOCKED).');
            END IF;
        END IF;
    END IF;
END;
/


-- ------------------------------------------------------------------------------------------
-- TRIGGER 3: trg_sync_bed_status
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: ADMISSION_REQUEST (Yêu cầu nhập viện)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: AFTER UPDATE (Sau khi cập nhật bộ dữ liệu thành công)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Trigger này tự động đồng bộ hóa trạng thái vật lý của giường bệnh dựa vào tình trạng nhập viện của bệnh nhân:
--     - Kiểm tra nếu có sự thay đổi trạng thái của yêu cầu nhập viện (:OLD.status != :NEW.status).
--     1. Nếu trạng thái mới chuyển thành 'ADMITTED' (Bệnh nhân đã nhập viện):
--        Tự động cập nhật giường bệnh tương ứng (bed_id = :NEW.bed_id) thành trạng thái 'OCCUPIED' (Đang có người dùng).
--     2. Nếu trạng thái mới chuyển thành 'DISCHARGED' (Bệnh nhân xuất viện) hoặc 'CANCELLED' (Yêu cầu nhập viện bị hủy):
--        Tự động cập nhật giường bệnh tương ứng thành trạng thái 'AVAILABLE' (Giường trống sẵn sàng phục vụ).
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_sync_bed_status
AFTER UPDATE ON ADMISSION_REQUEST
FOR EACH ROW
BEGIN
    IF :OLD.status != :NEW.status THEN
        IF :NEW.status = 'ADMITTED' THEN
            UPDATE BED SET status = 'OCCUPIED' WHERE bed_id = :NEW.bed_id;
        ELSIF :NEW.status IN ('DISCHARGED', 'CANCELLED') THEN
            UPDATE BED SET status = 'AVAILABLE' WHERE bed_id = :NEW.bed_id;
        END IF;
    END IF;
END;
/


-- ------------------------------------------------------------------------------------------
-- TRIGGER 4: trg_appointment_updated_at
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: APPOINTMENT (Lịch hẹn khám)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: BEFORE UPDATE (Trước khi cập nhật bộ dữ liệu)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Tự động theo dõi vết thời gian cập nhật của lịch hẹn. Mỗi khi có thao tác UPDATE dòng dữ liệu trên bảng
--     APPOINTMENT, trigger này sẽ tự động gán giá trị thời gian hệ thống hiện tại (SYSTIMESTAMP) vào thuộc tính
--     updated_at, đảm bảo tính chính xác và tự động hóa lưu vết kiểm toán (audit log).
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_appointment_updated_at
BEFORE UPDATE ON APPOINTMENT
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/


-- ------------------------------------------------------------------------------------------
-- TRIGGER 5: trg_med_record_updated_at
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: MEDICAL_RECORD (Hồ sơ bệnh án)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: BEFORE UPDATE (Trước khi cập nhật bộ dữ liệu)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Tự động theo dõi vết thời gian cập nhật của hồ sơ bệnh án. Mỗi khi bệnh án bị chỉnh sửa nội dung y khoa,
--     chẩn đoán, hoặc kết luận, trigger này tự động cập nhật trường updated_at của bảng MEDICAL_RECORD thành
--     thời gian hiện tại (SYSTIMESTAMP) của hệ thống.
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_med_record_updated_at
BEFORE UPDATE ON MEDICAL_RECORD
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/


-- ------------------------------------------------------------------------------------------
-- TRIGGER 6: trg_doc_schedule_updated_at
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: DOCTOR_SCHEDULE (Lịch trực/làm việc của bác sĩ)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: BEFORE UPDATE (Trước khi cập nhật bộ dữ liệu)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Theo dõi tự động vết thời gian của lịch làm việc bác sĩ. Mỗi khi cập nhật lịch trực (như thay đổi phòng trực,
--     ca trực, hoặc số lượng bệnh nhân đã đặt chỗ), trường updated_at của dòng tương ứng trong bảng DOCTOR_SCHEDULE
--     sẽ tự động ghi nhận thời gian hệ thống hiện hành (SYSTIMESTAMP).
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_doc_schedule_updated_at
BEFORE UPDATE ON DOCTOR_SCHEDULE
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/


-- ------------------------------------------------------------------------------------------
-- TRIGGER 7: trg_payment_record_updated_at
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: PAYMENT_RECORD (Hóa đơn / Giao dịch thanh toán y tế)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: BEFORE UPDATE (Trước khi cập nhật bộ dữ liệu)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Theo dõi tự động thời gian sửa đổi hóa đơn. Khi hóa đơn được cập nhật giá trị (tổng tiền điều trị thay đổi),
--     hoặc khi bệnh nhân thực hiện thanh toán chuyển trạng thái từ UNPAID sang PAID/PARTIAL, trigger tự động cập nhật
--     thuộc tính updated_at của bảng PAYMENT_RECORD thành SYSTIMESTAMP.
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_payment_record_updated_at
BEFORE UPDATE ON PAYMENT_RECORD
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSTIMESTAMP;
END;
/
