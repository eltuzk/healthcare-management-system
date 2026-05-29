-- ==========================================================================================
-- DEMO 4: MÔ PHỎNG LỖI INCONSISTENT WRITE (BỆNH ÁN HOÀN THÀNH - LỊCH HẸN KẸT IN_PROGRESS)
-- Tệp tin này mô phỏng lỗi tương tranh Inconsistent Write khi 2 kết quả y tế trả về đồng thời.
-- Ngôn ngữ: Oracle PL/SQL.
-- ==========================================================================================

-- ------------------------------------------------------------------------------------------
-- BƯỚC 1: THIẾT LẬP DỮ LIỆU THỬ NGHIỆM (SETUP)
-- Tạo bảng mô phỏng bệnh án, lịch hẹn và các chỉ định cận lâm sàng.
-- ------------------------------------------------------------------------------------------
CREATE TABLE APPOINTMENT_DEMO (
    appointment_id NUMBER PRIMARY KEY,
    status VARCHAR2(50) DEFAULT 'IN_PROGRESS'
);

CREATE TABLE MEDICAL_RECORD_DEMO (
    med_record_id NUMBER PRIMARY KEY,
    appointment_id NUMBER,
    status VARCHAR2(50) DEFAULT 'DRAFT'
);

CREATE TABLE SERVICE_RESULT_DEMO (
    result_id NUMBER PRIMARY KEY,
    appointment_id NUMBER,
    service_name VARCHAR2(100),
    status VARCHAR2(50) DEFAULT 'PENDING' -- PENDING: Chờ KQ, COMPLETED: Đã có KQ
);

-- Khởi tạo ca khám của bệnh nhân X: Gồm 2 chỉ định (Xét nghiệm máu & Chụp X-Quang)
INSERT INTO APPOINTMENT_DEMO (appointment_id, status) VALUES (100, 'IN_PROGRESS');
INSERT INTO MEDICAL_RECORD_DEMO (med_record_id, appointment_id, status) VALUES (500, 100, 'DRAFT');
INSERT INTO SERVICE_RESULT_DEMO (result_id, appointment_id, service_name, status) VALUES (1, 100, 'Xet nghiem mau', 'PENDING');
INSERT INTO SERVICE_RESULT_DEMO (result_id, appointment_id, service_name, status) VALUES (2, 100, 'Chup X-Quang', 'PENDING');
COMMIT;


-- ------------------------------------------------------------------------------------------
-- BƯỚC 2: MÔ PHỎNG LỖI CONCURRENCY (INCONSISTENT WRITE / STATE SYNC RACE)
-- Khi 2 kết quả xét nghiệm được gửi về song song cực kỳ đồng thời:
-- ------------------------------------------------------------------------------------------

/*
============================================================================================
[SESSION 1 - NHẬN KẾT QUẢ XÉT NGHIỆM MÁU]             [SESSION 2 - NHẬN KẾT QUẢ X-QUANG]
============================================================================================

-- S1 nhận kết quả y tế 1: cập nhật status thành COMPLETED
DECLARE
    v_pending_count NUMBER;
BEGIN
    -- Cập nhật kết quả xét nghiệm máu thành xong
    UPDATE SERVICE_RESULT_DEMO SET status = 'COMPLETED' WHERE result_id = 1;
    
    -- Kiểm tra xem còn dịch vụ cận lâm sàng nào đang chờ không để chốt lịch hẹn
    -- Chạy đồng thời ở mức Read Committed, S1 vẫn thấy kết quả 2 đang PENDING
    SELECT COUNT(*) INTO v_pending_count 
    FROM SERVICE_RESULT_DEMO 
    WHERE appointment_id = 100 AND status = 'PENDING';
    
    DBMS_OUTPUT.PUT_LINE('S1 quét kho dịch vụ: Còn ' || v_pending_count || ' dịch vụ chờ.');
    
    DBMS_LOCK.SLEEP(5); -- Giả lập độ trễ ghi chép
    
    -- Cập nhật hoàn tất hồ sơ bệnh án MEDICAL_RECORD
    UPDATE MEDICAL_RECORD_DEMO SET status = 'COMPLETED' WHERE med_record_id = 500;
    
    -- Vì v_pending_count = 1 > 0, S1 cho rằng đây chưa phải kết quả cuối cùng,
    -- không kích hoạt câu lệnh cập nhật trạng thái COMPLETED cho APPOINTMENT!
    
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('S1 hoàn tất và COMMIT!');
END;
/

                                                    -- S2 nhận kết quả y tế 2: cập nhật status thành COMPLETED
                                                    -- Chạy song song tại giây thứ 1
                                                    DECLARE
                                                        v_pending_count NUMBER;
                                                    BEGIN
                                                        -- Cập nhật kết quả X-Quang thành xong
                                                        UPDATE SERVICE_RESULT_DEMO SET status = 'COMPLETED' WHERE result_id = 2;
                                                        
                                                        -- Do S1 chưa commit, S2 quét bảng vẫn thấy kết quả 1 đang PENDING!
                                                        SELECT COUNT(*) INTO v_pending_count 
                                                        FROM SERVICE_RESULT_DEMO 
                                                        WHERE appointment_id = 100 AND status = 'PENDING';
                                                        
                                                        DBMS_OUTPUT.PUT_LINE('S2 quét kho dịch vụ: Còn ' || v_pending_count || ' dịch vụ chờ.');
                                                        
                                                        -- Cập nhật đè hoàn thành MEDICAL_RECORD
                                                        UPDATE MEDICAL_RECORD_DEMO SET status = 'COMPLETED' WHERE med_record_id = 500;
                                                        
                                                        -- Vì v_pending_count = 1 > 0, S2 cũng nghĩ chưa phải kết quả cuối cùng,
                                                        -- không kích hoạt câu lệnh cập nhật cho APPOINTMENT!
                                                        
                                                        COMMIT;
                                                        DBMS_OUTPUT.PUT_LINE('S2 hoàn tất và COMMIT!');
                                                    END;
                                                    /

============================================================================================
--> HẬU QUẢ SAU KHI CẢ HAI COMMIT:
SELECT status FROM MEDICAL_RECORD_DEMO WHERE med_record_id = 500; -- Kết quả: 'COMPLETED' (Đã đóng hồ sơ)
SELECT status FROM APPOINTMENT_DEMO WHERE appointment_id = 100;     -- Kết quả: 'IN_PROGRESS' (Bị kẹt!)
--> Dữ liệu bị lệch pha nghiêm trọng do cả 2 luồng đều kiểm tra chéo và bỏ sót bước đóng Appointment.
============================================================================================
*/


-- ------------------------------------------------------------------------------------------
-- BƯỚC 3: GIẢI PHÁP KHẮC PHỤC (SỬ DỤNG PESSIMISTIC LOCK TRÊN MEDICAL_RECORD ĐỂ ĐỒNG BỘ)
-- Chạy đoạn mã này để thấy cách khóa dòng bệnh án đồng bộ luồng kiểm tra:
-- ------------------------------------------------------------------------------------------

-- Phục hồi dữ liệu thử nghiệm ban đầu
UPDATE APPOINTMENT_DEMO SET status = 'IN_PROGRESS' WHERE appointment_id = 100;
UPDATE MEDICAL_RECORD_DEMO SET status = 'DRAFT' WHERE med_record_id = 500;
UPDATE SERVICE_RESULT_DEMO SET status = 'PENDING' WHERE result_id = 1;
UPDATE SERVICE_RESULT_DEMO SET status = 'PENDING' WHERE result_id = 2;
COMMIT;

/*
============================================================================================
[SESSION 1 - KẾT QUẢ MÁU VỚI LOCK]                     [SESSION 2 - KẾT QUẢ X-QUANG VỚI LOCK]
============================================================================================

-- S1 chạy trước:
DECLARE
    v_pending_count NUMBER;
    v_mr_id NUMBER;
BEGIN
    -- KHÓA CHẶT BỆNH ÁN CHA TRƯỚC KHI XỬ LÝ CHI TIẾT
    SELECT med_record_id INTO v_mr_id 
    FROM MEDICAL_RECORD_DEMO 
    WHERE med_record_id = 500 FOR UPDATE;
    
    -- Cập nhật kết quả xét nghiệm máu
    UPDATE SERVICE_RESULT_DEMO SET status = 'COMPLETED' WHERE result_id = 1;
    
    -- Quét số lượng còn lại
    SELECT COUNT(*) INTO v_pending_count 
    FROM SERVICE_RESULT_DEMO 
    WHERE appointment_id = 100 AND status = 'PENDING';
    
    DBMS_LOCK.SLEEP(5);
    
    IF v_pending_count = 0 THEN
        UPDATE MEDICAL_RECORD_DEMO SET status = 'COMPLETED' WHERE med_record_id = 500;
        UPDATE APPOINTMENT_DEMO SET status = 'COMPLETED' WHERE appointment_id = 100;
    END IF;
    
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('S1 hoàn tất có Lock và COMMIT!');
END;
/

                                                    -- S2 chạy song song tại giây thứ 1:
                                                    -- S2 gọi câu lệnh FOR UPDATE dưới đây và bị BLOCKED ngay lập tức.
                                                    -- S2 phải đợi S1 commit xong mới được đi tiếp.
                                                    DECLARE
                                                        v_pending_count NUMBER;
                                                        v_mr_id NUMBER;
                                                    BEGIN
                                                        SELECT med_record_id INTO v_mr_id 
                                                        FROM MEDICAL_RECORD_DEMO 
                                                        WHERE med_record_id = 500 FOR UPDATE; -- <--- S2 đợi ở đây
                                                        
                                                        -- Khi S2 được vào, v_pending_count kiểm tra sẽ bằng 0 
                                                        -- vì kết quả của S1 đã commit và cập nhật thành công!
                                                        UPDATE SERVICE_RESULT_DEMO SET status = 'COMPLETED' WHERE result_id = 2;
                                                        
                                                        SELECT COUNT(*) INTO v_pending_count 
                                                        FROM SERVICE_RESULT_DEMO 
                                                        WHERE appointment_id = 100 AND status = 'PENDING';
                                                        
                                                        DBMS_OUTPUT.PUT_LINE('S2 vào sau. Quét số lượng dịch vụ chờ = ' || v_pending_count);
                                                        
                                                        IF v_pending_count = 0 THEN
                                                            UPDATE MEDICAL_RECORD_DEMO SET status = 'COMPLETED' WHERE med_record_id = 500;
                                                            UPDATE APPOINTMENT_DEMO SET status = 'COMPLETED' WHERE appointment_id = 100;
                                                        END IF;
                                                        
                                                        COMMIT;
                                                        DBMS_OUTPUT.PUT_LINE('S2 hoàn tất và COMMIT!');
                                                    END;
                                                    /

============================================================================================
--> KẾT QUẢ TRONG DB SAU KHI CÓ GIẢI PHÁP:
SELECT status FROM MEDICAL_RECORD_DEMO WHERE med_record_id = 500; -- Kết quả: 'COMPLETED'
SELECT status FROM APPOINTMENT_DEMO WHERE appointment_id = 100;     -- Kết quả: 'COMPLETED' (Cả 2 đều hoàn tất thành công!)
============================================================================================
*/


-- ------------------------------------------------------------------------------------------
-- BƯỚC 4: DỌN DẸP DỮ LIỆU DEMO (CLEANUP)
-- ------------------------------------------------------------------------------------------
DROP TABLE SERVICE_RESULT_DEMO;
DROP TABLE MEDICAL_RECORD_DEMO;
DROP TABLE APPOINTMENT_DEMO;
