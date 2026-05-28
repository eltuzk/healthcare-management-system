-- ==========================================================================================
-- DEMO 1: MÔ PHỎNG LỖI LOST UPDATE TRÊN BẢNG DOCTOR_SCHEDULE (COUNT ĐẶT LỊCH)
-- Tệp tin này mô phỏng lỗi tương tranh Lost Update khi hai bệnh nhân đặt lịch cùng lúc.
-- Ngôn ngữ: Oracle PL/SQL.
-- ==========================================================================================

-- ------------------------------------------------------------------------------------------
-- BƯỚC 1: THIẾT LẬP DỮ LIỆU THỬ NGHIỆM (SETUP)
-- Chạy đoạn này để tạo bảng giả lập và chèn ca trực ban đầu có 8 người đặt (giới hạn tối đa 10).
-- ------------------------------------------------------------------------------------------
CREATE TABLE DOCTOR_SCHEDULE_DEMO (
    schedule_id VARCHAR2(50) PRIMARY KEY,
    doctor_name VARCHAR2(100),
    max_capacity NUMBER(3) DEFAULT 10,
    current_booking_count NUMBER(3) DEFAULT 0
);

INSERT INTO DOCTOR_SCHEDULE_DEMO (schedule_id, doctor_name, max_capacity, current_booking_count)
VALUES ('SCH_01', 'Dr. Anh', 10, 8);
COMMIT;


-- ------------------------------------------------------------------------------------------
-- BƯỚC 2: MÔ PHỎNG LỖI CONCURRENCY (LOST UPDATE)
-- Để mô phỏng chân thực, bạn hãy mở 2 cửa sổ Console (SQL Developer / SQL*Plus) 
-- đại diện cho 2 Session khác nhau và chạy tuần tự theo các bước thời gian (T1-T7):
-- ------------------------------------------------------------------------------------------

/*
============================================================================================
[SESSION 1 - BỆNH NHÂN A ĐẶT LỊCH]                    [SESSION 2 - BỆNH NHÂN B ĐẶT LỊCH]
============================================================================================

-- Thời điểm T1 (Session 1): Đọc số lượng hiện tại là 8
DECLARE
    v_count NUMBER;
BEGIN
    SELECT current_booking_count INTO v_count 
    FROM DOCTOR_SCHEDULE_DEMO 
    WHERE schedule_id = 'SCH_01';
    DBMS_OUTPUT.PUT_LINE('Session 1 đọc thấy current_booking_count = ' || v_count);
    
    -- Giả lập trễ mạng bằng cách ngủ 5 giây trước khi thực hiện ghi nhận
    DBMS_LOCK.SLEEP(5); 
    
    -- Thực hiện tăng số chỗ (8 + 1 = 9) và UPDATE
    UPDATE DOCTOR_SCHEDULE_DEMO 
    SET current_booking_count = v_count + 1 
    WHERE schedule_id = 'SCH_01';
    
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Session 1 đặt lịch thành công và COMMIT!');
END;
/

                                                    -- Thời điểm T2 (Session 2 - chạy song song tại giây thứ 1):
                                                    -- Do Session 1 chưa commit, Session 2 cũng đọc ra giá trị 8
                                                    DECLARE
                                                        v_count NUMBER;
                                                    BEGIN
                                                        SELECT current_booking_count INTO v_count 
                                                        FROM DOCTOR_SCHEDULE_DEMO 
                                                        WHERE schedule_id = 'SCH_01';
                                                        DBMS_OUTPUT.PUT_LINE('Session 2 đọc thấy current_booking_count = ' || v_count);
                                                        
                                                        -- Thực hiện tăng số chỗ (8 + 1 = 9) và UPDATE
                                                        -- Lệnh này sẽ bị TREO (Blocked) tại giây thứ 2 do Session 1 đang chiếm khóa dòng
                                                        UPDATE DOCTOR_SCHEDULE_DEMO 
                                                        SET current_booking_count = v_count + 1 
                                                        WHERE schedule_id = 'SCH_01';
                                                        
                                                        COMMIT;
                                                        DBMS_OUTPUT.PUT_LINE('Session 2 đặt lịch thành công sau khi S1 commit!');
                                                    END;
                                                    /

============================================================================================
--> KẾT QUẢ SAU KHI CẢ HAI COMMIT:
SELECT current_booking_count FROM DOCTOR_SCHEDULE_DEMO WHERE schedule_id = 'SCH_01';
--> Kết quả hiển thị: 9 (Sai nghiệp vụ, thực tế có 2 người đặt thành công phải là 10).
--> Nguyên nhân: Giao dịch của Session 2 đã ghi đè số 9 (tính từ giá trị 8 cũ) đè lên số 9 của Session 1.
============================================================================================
*/


-- ------------------------------------------------------------------------------------------
-- BƯỚC 3: GIẢI PHÁP KHẮC PHỤC (FIX BẰNG PESSIMISTIC LOCK - SELECT FOR UPDATE)
-- Chạy đoạn mã này để thấy cách khóa dòng ngăn chặn hoàn toàn Lost Update:
-- ------------------------------------------------------------------------------------------

-- Đưa dữ liệu demo về trạng thái ban đầu:
UPDATE DOCTOR_SCHEDULE_DEMO SET current_booking_count = 8 WHERE schedule_id = 'SCH_01';
COMMIT;

/*
============================================================================================
[SESSION 1 - SỬ DỤNG KHÓA BI QUAN]                    [SESSION 2 - SỬ DỤNG KHÓA BI QUAN]
============================================================================================

-- Thời điểm T1 (Session 1): Đọc và KHÓA dòng ca trực bằng SELECT FOR UPDATE
DECLARE
    v_count NUMBER;
    v_capacity NUMBER;
BEGIN
    SELECT current_booking_count, max_capacity INTO v_count, v_capacity
    FROM DOCTOR_SCHEDULE_DEMO 
    WHERE schedule_id = 'SCH_01'
    FOR UPDATE; -- <--- CHIẾM KHÓA DÒNG BI QUAN
    
    DBMS_OUTPUT.PUT_LINE('Session 1 đã khóa ca trực SCH_01. Current = ' || v_count);
    
    -- Giả lập trễ mạng
    DBMS_LOCK.SLEEP(5); 
    
    IF v_count < v_capacity THEN
        UPDATE DOCTOR_SCHEDULE_DEMO 
        SET current_booking_count = v_count + 1 
        WHERE schedule_id = 'SCH_01';
        COMMIT; -- <--- GIẢI PHÓNG KHÓA DÒNG
        DBMS_OUTPUT.PUT_LINE('Session 1 COMMIT thành công!');
    ELSE
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Ca khám đã đầy!');
    END IF;
END;
/

                                                    -- Thời điểm T2 (Session 2 - chạy song song tại giây thứ 1):
                                                    -- Session 2 gọi lệnh SELECT FOR UPDATE dưới đây.
                                                    -- Lập tức Session 2 bị BLOCKED (chờ xếp hàng) ngay tại câu SELECT.
                                                    -- S2 phải đợi cho đến khi Session 1 COMMIT ở giây thứ 5.
                                                    DECLARE
                                                        v_count NUMBER;
                                                        v_capacity NUMBER;
                                                    BEGIN
                                                        SELECT current_booking_count, max_capacity INTO v_count, v_capacity
                                                        FROM DOCTOR_SCHEDULE_DEMO 
                                                        WHERE schedule_id = 'SCH_01'
                                                        FOR UPDATE; -- <--- Phải đợi S1 giải phóng khóa mới được đi tiếp
                                                        
                                                        -- Khi S2 được vào, v_count lúc này đọc ra đã là 9 (chính xác!)
                                                        DBMS_OUTPUT.PUT_LINE('Session 2 vào sau khi S1 commit. Đọc thấy current = ' || v_count);
                                                        
                                                        IF v_count < v_capacity THEN
                                                            UPDATE DOCTOR_SCHEDULE_DEMO 
                                                            SET current_booking_count = v_count + 1 
                                                            WHERE schedule_id = 'SCH_01';
                                                            COMMIT;
                                                            DBMS_OUTPUT.PUT_LINE('Session 2 COMMIT thành công!');
                                                        ELSE
                                                            ROLLBACK;
                                                            DBMS_OUTPUT.PUT_LINE('Ca khám đã đầy!');
                                                        END IF;
                                                    END;
                                                    /

============================================================================================
--> KẾT QUẢ SAU KHI CÓ GIẢI PHÁP:
SELECT current_booking_count FROM DOCTOR_SCHEDULE_DEMO WHERE schedule_id = 'SCH_01';
--> Kết quả hiển thị: 10 (Chính xác 100% nghiệp vụ!).
============================================================================================
*/


-- ------------------------------------------------------------------------------------------
-- BƯỚC 4: DỌN DẸP DỮ LIỆU DEMO (CLEANUP)
-- ------------------------------------------------------------------------------------------
DROP TABLE DOCTOR_SCHEDULE_DEMO;
