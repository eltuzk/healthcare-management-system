-- ==========================================================================================
-- DEMO 5: MÔ PHỎNG LỖI NON-REPEATABLE READ TRÊN BẢNG APPOINTMENT (BÁC SĨ BẤM START SONG SONG)
-- Tệp tin này mô phỏng lỗi tương tranh Non-repeatable Read khi nút "Bắt đầu khám" bị bấm 2 lần song song.
-- Ngôn ngữ: Oracle PL/SQL.
-- ==========================================================================================

-- ------------------------------------------------------------------------------------------
-- BƯỚC 1: THIẾT LẬP DỮ LIỆU THỬ NGHIỆM (SETUP)
-- Tạo bảng mô phỏng lịch hẹn.
-- ------------------------------------------------------------------------------------------
CREATE TABLE APPOINTMENT_DEMO (
    appointment_id NUMBER PRIMARY KEY,
    status VARCHAR2(50) DEFAULT 'CHECKED_IN',
    start_time TIMESTAMP
);

INSERT INTO APPOINTMENT_DEMO (appointment_id, status, start_time) VALUES (100, 'CHECKED_IN', NULL);
COMMIT;


-- ------------------------------------------------------------------------------------------
-- BƯỚC 2: MÔ PHỎNG LỖI CONCURRENCY (NON-REPEATABLE READ)
-- Khi 2 request Bắt đầu khám được gửi đi đồng thời mà không khóa dòng:
-- ------------------------------------------------------------------------------------------

/*
============================================================================================
[SESSION 1 - REQUEST BẤM LẦN 1]                       [SESSION 2 - REQUEST BẤM LẦN 2]
============================================================================================

-- S1 chạy trước: Đọc thấy status là 'CHECKED_IN'
DECLARE
    v_status VARCHAR2(50);
BEGIN
    SELECT status INTO v_status FROM APPOINTMENT_DEMO WHERE appointment_id = 100;
    DBMS_OUTPUT.PUT_LINE('S1 đọc thấy status = ' || v_status);
    
    -- Giả lập độ trễ mạng
    DBMS_LOCK.SLEEP(5); 
    
    IF v_status = 'CHECKED_IN' THEN
        UPDATE APPOINTMENT_DEMO 
        SET status = 'IN_PROGRESS', 
            start_time = CURRENT_TIMESTAMP 
        WHERE appointment_id = 100;
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('S1 đổi trạng thái thành IN_PROGRESS thành công!');
    END IF;
END;
/

                                                    -- S2 chạy song song tại giây thứ 1:
                                                    -- S2 đọc thấy status vẫn là 'CHECKED_IN' (Chưa commit)
                                                    DECLARE
                                                        v_status VARCHAR2(50);
                                                    BEGIN
                                                        SELECT status INTO v_status FROM APPOINTMENT_DEMO WHERE appointment_id = 100;
                                                        DBMS_OUTPUT.PUT_LINE('S2 đọc thấy status = ' || v_status);
                                                        
                                                        -- S2 cũng thấy hợp lệ và tiến hành cập nhật ghi đè!
                                                        -- S2 bị blocked cho đến khi S1 commit
                                                        IF v_status = 'CHECKED_IN' THEN
                                                            UPDATE APPOINTMENT_DEMO 
                                                            SET status = 'IN_PROGRESS', 
                                                                start_time = CURRENT_TIMESTAMP 
                                                            WHERE appointment_id = 100;
                                                            COMMIT;
                                                            DBMS_OUTPUT.PUT_LINE('S2 ghi đè thành công!');
                                                        END IF;
                                                    END;
                                                    /

============================================================================================
--> HẬU QUẢ: Cả hai giao dịch cùng chạy thành công. S2 đã ghi đè hoàn toàn thời gian start_time
--> của S1, dù thực tế ca khám đã được S1 khởi chạy hợp lệ từ trước.
============================================================================================
*/


-- ------------------------------------------------------------------------------------------
-- BƯỚC 3: GIẢI PHÁP KHẮC PHỤC (SỬ DỤNG PESSIMISTIC LOCK TRÊN DÒNG APPOINTMENT)
-- Chạy đoạn mã này để thấy cách khóa dòng ngăn chặn hoàn toàn lỗi cập nhật trùng lặp:
-- ------------------------------------------------------------------------------------------

-- Đưa dữ liệu demo về trạng thái ban đầu:
UPDATE APPOINTMENT_DEMO SET status = 'CHECKED_IN', start_time = NULL WHERE appointment_id = 100;
COMMIT;

/*
============================================================================================
[SESSION 1 - START CÓ LOCK BI QUAN]                    [SESSION 2 - START CÓ LOCK BI QUAN]
============================================================================================

-- S1 chạy trước: Khóa dòng bằng SELECT FOR UPDATE
DECLARE
    v_status VARCHAR2(50);
BEGIN
    SELECT status INTO v_status 
    FROM APPOINTMENT_DEMO 
    WHERE appointment_id = 100 
    FOR UPDATE; -- <--- KHÓA DÒNG BI QUAN
    
    DBMS_OUTPUT.PUT_LINE('S1 đã chiếm khóa dòng. Status = ' || v_status);
    
    DBMS_LOCK.SLEEP(5); 
    
    IF v_status = 'CHECKED_IN' THEN
        UPDATE APPOINTMENT_DEMO 
        SET status = 'IN_PROGRESS', 
            start_time = CURRENT_TIMESTAMP 
        WHERE appointment_id = 100;
        COMMIT; -- <--- Giải phóng khóa
        DBMS_OUTPUT.PUT_LINE('S1 COMMIT thành công!');
    ELSE
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Lịch khám không ở trạng thái hợp lệ!');
    END IF;
END;
/

                                                    -- S2 chạy song song tại giây thứ 1:
                                                    -- S2 gọi câu lệnh dưới đây và bị BLOCKED ngay tại câu SELECT.
                                                    -- S2 phải xếp hàng đợi cho đến khi S1 commit ở giây thứ 5.
                                                    DECLARE
                                                        v_status VARCHAR2(50);
                                                    BEGIN
                                                        SELECT status INTO v_status 
                                                        FROM APPOINTMENT_DEMO 
                                                        WHERE appointment_id = 100 
                                                        FOR UPDATE; -- <--- S2 đợi ở đây
                                                        
                                                        -- Khi S2 được vào, trạng thái mới nhất nạp lên đã là 'IN_PROGRESS'
                                                        DBMS_OUTPUT.PUT_LINE('S2 vào sau. Trạng thái mới nhất = ' || v_status);
                                                        
                                                        -- Ràng buộc này sẽ chặn đứng lệnh ghi đè của S2 vì status không còn là CHECKED_IN
                                                        IF v_status = 'CHECKED_IN' THEN
                                                            UPDATE APPOINTMENT_DEMO 
                                                            SET status = 'IN_PROGRESS', 
                                                                start_time = CURRENT_TIMESTAMP 
                                                            WHERE appointment_id = 100;
                                                            COMMIT;
                                                            DBMS_OUTPUT.PUT_LINE('S2 ghi đè!');
                                                        ELSE
                                                            ROLLBACK;
                                                            DBMS_OUTPUT.PUT_LINE('CHẶN LỖI: Ca khám này đã được bắt đầu từ trước!');
                                                        END IF;
                                                    END;
                                                    /

============================================================================================
--> KẾT QUẢ SAU KHI CÓ GIẢI PHÁP:
--> Lịch khám được bắt đầu an toàn bởi S1. 
--> Yêu cầu bấm trùng lần thứ hai từ S2 bị chặn đứng bởi logic xác thực sau khóa bi quan.
============================================================================================
*/


-- ------------------------------------------------------------------------------------------
-- BƯỚC 4: DỌN DẸP DỮ LIỆU DEMO (CLEANUP)
-- ------------------------------------------------------------------------------------------
DROP TABLE APPOINTMENT_DEMO;
