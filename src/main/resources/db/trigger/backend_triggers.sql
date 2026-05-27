-- ==========================================================================================
-- NHÓM 2: CÁC TRIGGER NGHIỆP VỤ ĐANG TRIỂN KHAI Ở BẬC BACKEND (APPLICATION-LEVEL TRIGGERS)
-- Tệp tin này tổng hợp 8 trigger nghiệp vụ hiện đang hoạt động tại mã nguồn Java của backend.
-- Nhằm mục đích nhất quán và đồng bộ, tôi đã thiết kế và biên dịch logic Java đó thành mã 
-- Database Triggers tương ứng (sử dụng cú pháp Oracle PL/SQL đồng bộ với hệ thống CSDL).
-- Mỗi trigger đều có đánh số thứ tự tiếp theo và phần mô tả chi tiết bằng Tiếng Việt có dấu.
-- ==========================================================================================

-- ------------------------------------------------------------------------------------------
-- TRIGGER 8: trg_reserve_doctor_schedule_slot
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: APPOINTMENT (Lịch hẹn khám)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: AFTER INSERT (Sau khi đặt lịch hẹn thành công)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] LOGIC BACKEND TƯƠNG ĐƯƠNG: reserveDoctorScheduleSlot() trong AppointmentServiceImpl.java
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Trigger này tự động hóa việc giữ chỗ lịch trực của bác sĩ khi bệnh nhân đặt lịch hẹn thành công:
--     - Khi có bản ghi APPOINTMENT mới được chèn với trạng thái 'PENDING' hoặc 'CHECKED_IN':
--       Tự động cập nhật bảng DOCTOR_SCHEDULE của ca trực tương ứng:
--       1. Tăng last_queue_number lên 1 (last_queue_number = last_queue_number + 1) để cấp số thứ tự khám tiếp theo.
--       2. Tăng current_booking_count lên 1 (current_booking_count = current_booking_count + 1) để giữ chỗ khám.
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_reserve_doctor_schedule_slot
AFTER INSERT ON APPOINTMENT
FOR EACH ROW
BEGIN
    IF :NEW.status IN ('PENDING', 'CHECKED_IN') THEN
        UPDATE DOCTOR_SCHEDULE 
        SET last_queue_number = last_queue_number + 1,
            current_booking_count = current_booking_count + 1
        WHERE doctor_schedule_id = :NEW.doctor_schedule_id;
    END IF;
END;
/


-- ------------------------------------------------------------------------------------------
-- TRIGGER 9: trg_release_doctor_schedule_slot
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: APPOINTMENT (Lịch hẹn khám)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: AFTER UPDATE (Sau khi cập nhật trạng thái lịch hẹn)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] LOGIC BACKEND TƯƠNG ĐƯƠNG: releaseReservedDoctorScheduleSlot() trong AppointmentServiceImpl.java
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Trigger này tự động hóa việc giải phóng vị trí phòng khám trống khi lịch hẹn bị hủy bỏ:
--     - Khi trạng thái lịch hẹn thay đổi từ 'PENDING' sang 'CANCELLED' (Đã hủy) hoặc 'PAYMENT_EXPIRED' (Hết hạn thanh toán):
--       Tự động trừ bớt số lượng đặt chỗ hiện tại trong ca trực của bác sĩ tương ứng (current_booking_count - 1),
--       giúp bác sĩ giải phóng được thời gian trống để tiếp nhận các bệnh nhân khác.
--       Sử dụng hàm GREATEST để đảm bảo số chỗ không bị âm (< 0).
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_release_doctor_schedule_slot
AFTER UPDATE ON APPOINTMENT
FOR EACH ROW
BEGIN
    IF :OLD.status = 'PENDING' AND :NEW.status IN ('CANCELLED', 'PAYMENT_EXPIRED') THEN
        UPDATE DOCTOR_SCHEDULE
        SET current_booking_count = GREATEST(0, current_booking_count - 1)
        WHERE doctor_schedule_id = :NEW.doctor_schedule_id;
    END IF;
END;
/

-- ------------------------------------------------------------------------------------------
-- TRIGGER 10: trg_appointment_fee_snapshot
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: APPOINTMENT (Lịch hẹn khám)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: BEFORE INSERT (Trước khi chèn lịch hẹn mới)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] LOGIC BACKEND TƯƠNG ĐƯƠNG: populateAppointmentForBooking() trong AppointmentServiceImpl.java
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Trigger này giúp sao chụp giá tiền và tên dịch vụ tại thời điểm đăng ký khám nhằm lưu vết lịch sử hóa đơn:
--     - Trước khi lưu lịch hẹn, hệ thống tìm giá trị đơn giá khám hiện tại của chuyên khoa của bác sĩ/hoặc theo fee_id:
--       1. Nếu fee_id được truyền: Lấy trực tiếp price và fee_name từ bảng CONSULTATION_FEE.
--       2. Nếu không: Tự động tra cứu từ chuyên khoa của bác sĩ phụ trách ca trực.
--     - Gán giá trị lấy được vào trường fee_price_snapshot và fee_name_snapshot của lịch hẹn mới chèn.
--     - Giúp hóa đơn không bị ảnh hưởng nếu bảng giá danh mục dịch vụ thay đổi trong tương lai.
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_appointment_fee_snapshot
BEFORE INSERT ON APPOINTMENT
FOR EACH ROW
DECLARE
    v_price NUMBER(15, 2);
    v_fee_name VARCHAR2(255);
BEGIN
    IF :NEW.fee_id IS NOT NULL THEN
        SELECT price, fee_name INTO v_price, v_fee_name
        FROM CONSULTATION_FEE
        WHERE fee_id = :NEW.fee_id;
    ELSE
        -- Tự động phân giải đơn giá dựa trên chuyên khoa của bác sĩ được xếp lịch trực
        SELECT cf.price, cf.fee_name INTO v_price, v_fee_name
        FROM CONSULTATION_FEE cf
        INNER JOIN DOCTOR_SCHEDULE ds ON ds.doctor_schedule_id = :NEW.doctor_schedule_id
        INNER JOIN DOCTOR d ON d.doctor_id = ds.doctor_id
        WHERE cf.specialty_id = d.specialty_id AND cf.is_active = 1 AND ROWNUM = 1;
    END IF;
    
    :NEW.fee_price_snapshot := v_price;
    :NEW.fee_name_snapshot := v_fee_name;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20101, 'Không tìm thấy đơn giá khám phù hợp trong danh mục phí khám.');
END;
/


-- ------------------------------------------------------------------------------------------
-- TRIGGER 11: trg_init_appointment_payment
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: APPOINTMENT (Lịch hẹn khám)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: AFTER INSERT (Sau khi lưu thành công lịch hẹn mới)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] LOGIC BACKEND TƯƠNG ĐƯƠNG: initializePaymentRecord() trong AppointmentServiceImpl.java
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Trigger này tự động hóa việc khởi tạo hồ sơ công nợ / hóa đơn tài chính ngay khi đặt lịch khám:
--     - Ngay sau khi một cuộc hẹn khám (APPOINTMENT) được khởi tạo thành công:
--       Tự động thêm mới một dòng hóa đơn tương ứng vào bảng PAYMENT_RECORD.
--       Trường total_price được lấy chính xác từ đơn giá khám snapshot của lịch khám y tế đó.
--       Đặt số tiền đã thu (received_amount) mặc định bằng 0 và trạng thái thanh toán mặc định là 'UNPAID'.
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_init_appointment_payment
AFTER INSERT ON APPOINTMENT
FOR EACH ROW
BEGIN
    INSERT INTO PAYMENT_RECORD (
        payment_record_id,
        appointment_id,
        request_code,
        total_price,
        received_amount,
        payment_status,
        created_at,
        updated_at
    ) VALUES (
        payment_record_seq.NEXTVAL,
        :NEW.appointment_id,
        :NEW.appointment_code,
        :NEW.fee_price_snapshot,
        0.00,
        'UNPAID',
        SYSTIMESTAMP,
        SYSTIMESTAMP
    );
END;
/


-- ------------------------------------------------------------------------------------------
-- TRIGGER 12: trg_complete_appointment_on_mr_complete
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: MEDICAL_RECORD (Hồ sơ bệnh án)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: AFTER UPDATE (Sau khi cập nhật thành công trạng thái bệnh án)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] LOGIC BACKEND TƯƠNG ĐƯƠNG: complete() trong MedicalRecordServiceImpl.java
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Trigger này tự động đồng bộ hóa trạng thái hoàn thành giữa Hồ sơ bệnh án và Lịch hẹn khám liên đới:
--     - Khi trạng thái y khoa của bệnh án được bác sĩ cập nhật từ 'DRAFT'/'IN_PROGRESS' sang 'COMPLETED' (Đã hoàn tất):
--       Tự động kích hoạt câu lệnh cập nhật trạng thái của Lịch hẹn khám (APPOINTMENT) tương ứng thành 'COMPLETED'.
--       Giúp đóng lại vòng đời của ca khám bệnh mà không cần bác sĩ hay nhân viên y tế thao tác cập nhật thủ công hai lần.
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_complete_appointment_on_mr_complete
AFTER UPDATE ON MEDICAL_RECORD
FOR EACH ROW
BEGIN
    IF :OLD.status != 'COMPLETED' AND :NEW.status = 'COMPLETED' THEN
        UPDATE APPOINTMENT
        SET status = 'COMPLETED',
            updated_at = SYSTIMESTAMP
        WHERE appointment_id = :NEW.appointment_id;
    END IF;
END;
/


-- ------------------------------------------------------------------------------------------
-- TRIGGER 13: trg_sync_medical_record_billing
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: LAB_TEST_REQUEST_ITEM & MEDICAL_SERVICE_REQUEST_ITEM (Dịch vụ chỉ định & Xét nghiệm cận lâm sàng)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: AFTER INSERT OR UPDATE OR DELETE (Sau khi thêm/sửa/xóa dịch vụ y tế chỉ định)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] LOGIC BACKEND TƯƠNG ĐƯƠNG: syncBilling() trong MedicalRecordBillingServiceImpl.java
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Trigger này tự động tính toán lại chi phí của toàn bộ bệnh án mỗi khi có dịch vụ hoặc xét nghiệm y tế phát sinh:
--     1. Khi thêm, bớt hoặc điều chỉnh giá dịch vụ/xét nghiệm (ở LAB_TEST_REQUEST_ITEM hoặc MEDICAL_SERVICE_REQUEST_ITEM):
--        Tự động tính tổng tiền (SUM) của toàn bộ các xét nghiệm cận lâm sàng và dịch vụ y khoa đang chỉ định cho hồ sơ bệnh án.
--     2. Tiến hành cập nhật giá trị tổng chi phí điều trị này vào trường total_price của bảng MEDICAL_RECORD.
--     3. Tự động đồng bộ hóa sang hóa đơn PAYMENT_RECORD tương ứng (cập nhật lại total_price và tính toán lại
--        trạng thái thanh toán mới của hóa đơn PAYMENT_STATUS là UNPAID/PARTIAL/PAID dựa trên số tiền thực tế đã thu được).
-- ------------------------------------------------------------------------------------------
-- Phân đoạn A: Lắng nghe sự thay đổi cận lâm sàng từ bảng LAB_TEST_REQUEST_ITEM
CREATE OR REPLACE TRIGGER trg_sync_lab_billing
AFTER INSERT OR UPDATE OR DELETE ON LAB_TEST_REQUEST_ITEM
FOR EACH ROW
DECLARE
    v_med_record_id NUMBER(19);
    v_total_price NUMBER(15, 2);
    v_received NUMBER(15, 2);
BEGIN
    IF INSERTING OR UPDATING THEN
        SELECT med_record_id INTO v_med_record_id
        FROM LAB_TEST_REQUEST
        WHERE lab_test_request_id = :NEW.lab_test_request_id;
    ELSE
        SELECT med_record_id INTO v_med_record_id
        FROM LAB_TEST_REQUEST
        WHERE lab_test_request_id = :OLD.lab_test_request_id;
    END IF;
    
    -- Tính toán tổng tiền mới (tổng dịch vụ + tổng xét nghiệm của bệnh án)
    SELECT NVL(SUM(item.snapshot_price), 0) INTO v_total_price
    FROM LAB_TEST_REQUEST_ITEM item
    INNER JOIN LAB_TEST_REQUEST req ON req.lab_test_request_id = item.lab_test_request_id
    WHERE req.med_record_id = v_med_record_id;
    
    DECLARE
        v_service_total NUMBER(15, 2);
    BEGIN
        SELECT NVL(SUM(item.snapshot_price), 0) INTO v_service_total
        FROM MEDICAL_SERVICE_REQUEST_ITEM item
        INNER JOIN MEDICAL_SERVICE_REQUEST req ON req.med_ser_req_id = item.med_ser_req_id
        WHERE req.med_record_id = v_med_record_id;
        v_total_price := v_total_price + v_service_total;
    END;

    -- Cập nhật tổng chi phí điều trị tại MEDICAL_RECORD
    UPDATE MEDICAL_RECORD
    SET total_price = v_total_price
    WHERE med_record_id = v_med_record_id;

    -- Kiểm tra sự tồn tại và lấy số tiền đã thanh toán của hóa đơn bệnh án tương ứng
    BEGIN
        SELECT NVL(received_amount, 0) INTO v_received
        FROM PAYMENT_RECORD
        WHERE med_record_id = v_med_record_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_received := 0;
            INSERT INTO PAYMENT_RECORD (payment_record_id, med_record_id, request_code, total_price, received_amount, payment_status, created_at, updated_at)
            VALUES (payment_record_seq.NEXTVAL, v_med_record_id, 'MR-' || v_med_record_id, v_total_price, 0.00, 'UNPAID', SYSTIMESTAMP, SYSTIMESTAMP);
    END;

    -- Cập nhật tổng tiền và trạng thái thanh toán mới của PAYMENT_RECORD
    UPDATE PAYMENT_RECORD
    SET total_price = v_total_price,
        payment_status = CASE 
            WHEN v_total_price = 0 OR v_received = 0 THEN 'UNPAID'
            WHEN v_received < v_total_price THEN 'PARTIAL'
            ELSE 'PAID'
        END
    WHERE med_record_id = v_med_record_id;
END;
/

-- Phân đoạn B: Lắng nghe sự thay đổi cận lâm sàng từ bảng MEDICAL_SERVICE_REQUEST_ITEM
CREATE OR REPLACE TRIGGER trg_sync_service_billing
AFTER INSERT OR UPDATE OR DELETE ON MEDICAL_SERVICE_REQUEST_ITEM
FOR EACH ROW
DECLARE
    v_med_record_id NUMBER(19);
    v_total_price NUMBER(15, 2);
    v_received NUMBER(15, 2);
BEGIN
    IF INSERTING OR UPDATING THEN
        SELECT med_record_id INTO v_med_record_id
        FROM MEDICAL_SERVICE_REQUEST
        WHERE med_ser_req_id = :NEW.med_ser_req_id;
    ELSE
        SELECT med_record_id INTO v_med_record_id
        FROM MEDICAL_SERVICE_REQUEST
        WHERE med_ser_req_id = :OLD.med_ser_req_id;
    END IF;
    
    -- Tính toán tổng tiền mới (tổng dịch vụ + tổng xét nghiệm của bệnh án)
    SELECT NVL(SUM(item.snapshot_price), 0) INTO v_total_price
    FROM MEDICAL_SERVICE_REQUEST_ITEM item
    INNER JOIN MEDICAL_SERVICE_REQUEST req ON req.med_ser_req_id = item.med_ser_req_id
    WHERE req.med_record_id = v_med_record_id;
    
    DECLARE
        v_lab_total NUMBER(15, 2);
    BEGIN
        SELECT NVL(SUM(item.snapshot_price), 0) INTO v_lab_total
        FROM LAB_TEST_REQUEST_ITEM item
        INNER JOIN LAB_TEST_REQUEST req ON req.lab_test_request_id = item.lab_test_request_id
        WHERE req.med_record_id = v_med_record_id;
        v_total_price := v_total_price + v_lab_total;
    END;

    -- Cập nhật tổng chi phí điều trị tại MEDICAL_RECORD
    UPDATE MEDICAL_RECORD
    SET total_price = v_total_price
    WHERE med_record_id = v_med_record_id;

    -- Kiểm tra sự tồn tại và lấy số tiền đã thanh toán của hóa đơn bệnh án tương ứng
    BEGIN
        SELECT NVL(received_amount, 0) INTO v_received
        FROM PAYMENT_RECORD
        WHERE med_record_id = v_med_record_id;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_received := 0;
            INSERT INTO PAYMENT_RECORD (payment_record_id, med_record_id, request_code, total_price, received_amount, payment_status, created_at, updated_at)
            VALUES (payment_record_seq.NEXTVAL, v_med_record_id, 'MR-' || v_med_record_id, v_total_price, 0.00, 'UNPAID', SYSTIMESTAMP, SYSTIMESTAMP);
    END;

    -- Cập nhật tổng tiền và trạng thái thanh toán mới của PAYMENT_RECORD
    UPDATE PAYMENT_RECORD
    SET total_price = v_total_price,
        payment_status = CASE 
            WHEN v_total_price = 0 OR v_received = 0 THEN 'UNPAID'
            WHEN v_received < v_total_price THEN 'PARTIAL'
            ELSE 'PAID'
        END
    WHERE med_record_id = v_med_record_id;
END;
/


-- ------------------------------------------------------------------------------------------
-- TRIGGER 14: trg_sync_prescription_billing
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: PRESCRIPTION_DETAIL (Chi tiết thuốc trong đơn thuốc)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: AFTER INSERT OR UPDATE OR DELETE (Sau khi thêm/sửa/xóa dòng chi tiết thuốc)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] LOGIC BACKEND TƯƠNG ĐƯƠNG: createOrUpdatePrescriptionPayment() trong PrescriptionServiceImpl.java
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Trigger này tự động hóa việc tính toán giá trị hóa đơn tiền thuốc mỗi khi có thuốc được kê thêm hoặc sửa bớt số lượng:
--     - Khi thay đổi danh sách thuốc y tế của đơn thuốc (kê đơn mới hoặc chỉnh sửa):
--       1. Tính tổng hóa đơn thuốc mới bằng cách lấy số lượng (quantity) nhân với đơn giá bán hiện thời của loại thuốc đó
--          (m.selling_price trong bảng MEDICINE) cho toàn bộ chi tiết thuốc của đơn.
--       2. Tìm bản ghi hóa đơn thuốc PAYMENT_RECORD liên kết tương ứng.
--       3. Nếu hóa đơn chưa ở trạng thái hoàn thành thanh toán ('PAID'), tiến hành tự động cập nhật total_price
--          đồng thời tính toán lại trạng thái thanh toán mới của hóa đơn đơn thuốc (UNPAID/PARTIAL/PAID) một cách tự động.
--       4. Nếu chưa tồn tại hóa đơn thuốc, trigger sẽ tự động chèn mới một hóa đơn tiền thuốc với trạng thái 'UNPAID'.
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_sync_prescription_billing
AFTER INSERT OR UPDATE OR DELETE ON PRESCRIPTION_DETAIL
FOR EACH ROW
DECLARE
    v_prescription_id NUMBER(19);
    v_total_price NUMBER(15, 2);
    v_received NUMBER(15, 2);
    v_payment_status VARCHAR2(20);
BEGIN
    IF INSERTING OR UPDATING THEN
        v_prescription_id := :NEW.prescription_id;
    ELSE
        v_prescription_id := :OLD.prescription_id;
    END IF;

    -- Tính tổng chi phí thuốc của đơn thuốc (quantity * selling_price)
    SELECT NVL(SUM(pd.quantity * m.selling_price), 0) INTO v_total_price
    FROM PRESCRIPTION_DETAIL pd
    INNER JOIN MEDICINE m ON m.medicine_id = pd.medicine_id
    WHERE pd.prescription_id = v_prescription_id;

    -- Tra cứu hóa đơn của đơn thuốc tương ứng
    BEGIN
        SELECT NVL(received_amount, 0), payment_status INTO v_received, v_payment_status
        FROM PAYMENT_RECORD
        WHERE prescription_id = v_prescription_id;

        -- Nếu hóa đơn chưa được thanh toán thành công hoàn toàn, cập nhật lại giá tiền
        IF v_payment_status != 'PAID' THEN
            UPDATE PAYMENT_RECORD
            SET total_price = v_total_price,
                payment_status = CASE 
                    WHEN v_total_price = 0 OR v_received = 0 THEN 'UNPAID'
                    WHEN v_received < v_total_price THEN 'PARTIAL'
                    ELSE 'PAID'
                END
            WHERE prescription_id = v_prescription_id;
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            -- Tạo hóa đơn tiền thuốc mới nếu chưa tồn tại
            INSERT INTO PAYMENT_RECORD (
                payment_record_id, prescription_id, request_code, total_price, received_amount, payment_status, created_at, updated_at
            ) VALUES (
                payment_record_seq.NEXTVAL, v_prescription_id, 'PR-' || v_prescription_id, v_total_price, 0.00, 'UNPAID', SYSTIMESTAMP, SYSTIMESTAMP
            );
    END;
END;
/


-- ------------------------------------------------------------------------------------------
-- TRIGGER 15: trg_prescription_fifo_stock_deduction
-- ------------------------------------------------------------------------------------------
-- [*] BẢNG TÁC ĐỘNG: PRESCRIPTION (Đơn thuốc)
-- [*] THỜI ĐIỂM & SỰ KIỆN KÍCH HOẠT: AFTER UPDATE (Sau khi cập nhật trạng thái đơn thuốc)
-- [*] PHẠM VI XỬ LÝ: FOR EACH ROW (Mỗi dòng dữ liệu bị ảnh hưởng)
-- [*] LOGIC BACKEND TƯƠNG ĐƯƠNG: dispensePrescription() trong PrescriptionServiceImpl.java
-- [*] MÔ TẢ & CÔNG DỤNG:
--     Trigger này tự động hóa quy trình quản lý tồn kho thuốc theo chuẩn nguyên tắc FIFO (First Expired, First Out):
--     - Lắng nghe khi trạng thái hoạt động của đơn thuốc thay đổi từ đang hoạt động sang đã phát (is_active cập nhật từ 1 về 0).
--     - Với mỗi loại thuốc được kê đơn, trigger lấy ra danh sách các lô thuốc MEDICINE_LOT còn hạn sử dụng (không bị hết hạn)
--       và sắp xếp theo thứ tự ngày hết hạn tăng dần (lô thuốc nào hết hạn trước thì ưu tiên xuất trước - FIFO).
--     - Tự động trừ dần số lượng tồn kho của các lô thuốc:
--       1. Nếu số lượng của lô lớn hơn số lượng thuốc cần xuất: Trực tiếp khấu trừ số lượng lô và dừng quy trình.
--       2. Nếu không đủ: Trừ hết số lượng của lô về 0 và tiếp tục chuyển sang khấu trừ ở các lô tiếp theo cho tới khi đủ.
--     - Nếu duyệt hết các lô khả dụng mà vẫn không đủ số lượng để xuất, tự động kích hoạt lỗi nghiệp vụ (-20102) 
--       để chặn giao dịch và thông báo cho dược sĩ.
-- ------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_prescription_fifo_stock_deduction
AFTER UPDATE ON PRESCRIPTION
FOR EACH ROW
DECLARE
    v_remaining_deduct NUMBER(10);
    CURSOR c_lots(cp_medicine_id NUMBER) IS
        SELECT medicine_lot_id, quantity
        FROM MEDICINE_LOT
        WHERE medicine_id = cp_medicine_id 
          AND is_active = 1 
          AND quantity > 0 
          AND (expiry_date IS NULL OR expiry_date >= TRUNC(SYSDATE))
        ORDER BY expiry_date ASC NULLS LAST; -- FIFO: Lô hết hạn trước xuất trước
BEGIN
    -- Chỉ kích hoạt khi đơn thuốc đổi trạng thái sang đã được phát (is_active chuyển từ 1 về 0)
    IF :OLD.is_active = 1 AND :NEW.is_active = 0 THEN
        -- Duyệt qua tất cả các chi tiết dược phẩm trong đơn thuốc
        FOR rec_detail IN (
            SELECT medicine_id, quantity 
            FROM PRESCRIPTION_DETAIL 
            WHERE prescription_id = :NEW.prescription_id
        ) LOOP
            v_remaining_deduct := rec_detail.quantity;
            
            -- Khấu trừ số lượng theo phương pháp FIFO
            FOR rec_lot IN c_lots(rec_detail.medicine_id) LOOP
                EXIT WHEN v_remaining_deduct <= 0;
                
                IF rec_lot.quantity >= v_remaining_deduct THEN
                    UPDATE MEDICINE_LOT
                    SET quantity = quantity - v_remaining_deduct
                    WHERE medicine_lot_id = rec_lot.medicine_lot_id;
                    
                    v_remaining_deduct := 0;
                ELSE
                    UPDATE MEDICINE_LOT
                    SET quantity = 0
                    WHERE medicine_lot_id = rec_lot.medicine_lot_id;
                    
                    v_remaining_deduct := v_remaining_deduct - rec_lot.quantity;
                END IF;
            END LOOP;
            
            -- Nếu sau khi quét toàn bộ các lô thuốc mà số lượng trừ vẫn chưa đủ
            IF v_remaining_deduct > 0 THEN
                RAISE_APPLICATION_ERROR(-20102, 'Lỗi cấp phát: Dược phẩm ID ' || rec_detail.medicine_id || ' không đủ tồn kho khả dụng để thực hiện trừ kho theo nguyên lý FIFO.');
            END IF;
        END LOOP;
    END IF;
END;
/
