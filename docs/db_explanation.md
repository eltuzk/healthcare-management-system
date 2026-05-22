# Mô tả chi tiết Cơ sở dữ liệu — Hệ thống Quản lý Bệnh viện

**Tổng cộng: 38 bảng** · DBMS: Oracle · Trạng thái cuối cùng sau migration V1 → V26.

> **Quy ước:**
> - **PK** = Primary Key · **FK** = Foreign Key · **UQ** = Unique · **NN** = Not Null
> - **IDENTITY** = Tự tăng

---

## 1. Phân hệ Tài khoản & Phân quyền

---

### Bảng 1: ROLE — Vai trò

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | role_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của vai trò, được hệ thống tự động sinh |
| 2 | role_name | VARCHAR2(100) | NN, UQ | Tên vai trò dùng để phân loại người dùng trong hệ thống, ví dụ DOCTOR, PATIENT, RECEPTIONIST |
| 3 | description | VARCHAR2(500) | | Mô tả chi tiết chức năng và phạm vi hoạt động của vai trò |

---

### Bảng 2: PERMISSION — Quyền hạn

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | permission_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của quyền hạn, được hệ thống tự động sinh |
| 2 | permission_name | VARCHAR2(100) | NN, UQ | Tên quyền dùng để kiểm soát truy cập chức năng, ví dụ VIEW_PATIENT, EDIT_RECORD |
| 3 | detail | VARCHAR2(500) | | Mô tả chi tiết phạm vi và hành vi mà quyền này cho phép thực hiện |

---

### Bảng 3: ROLE_PERMISSION — Phân quyền theo vai trò

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | role_id | NUMBER | PK, FK → ROLE | Tham chiếu đến vai trò được gán quyền |
| 2 | permission_id | NUMBER | PK, FK → PERMISSION | Tham chiếu đến quyền hạn được gán cho vai trò |

> Bảng trung gian thể hiện quan hệ nhiều-nhiều giữa ROLE và PERMISSION. Khoá chính kép gồm role_id và permission_id.

---

### Bảng 4: ACCOUNT — Tài khoản đăng nhập

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | account_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của tài khoản, được hệ thống tự động sinh |
| 2 | email | VARCHAR2(255) | NN, UQ | Địa chỉ email dùng làm tên đăng nhập, mỗi email chỉ đăng ký một tài khoản |
| 3 | password_hash | VARCHAR2(255) | | Mật khẩu đã được mã hoá bằng thuật toán băm. Cho phép null đối với tài khoản đăng nhập bằng Google |
| 4 | role_id | NUMBER | NN, FK → ROLE | Vai trò được gán cho tài khoản, quyết định nhóm quyền mặc định |
| 5 | is_active | NUMBER(1) | NN, DEFAULT 1, CHECK IN (0, 1) | Cờ đánh dấu tài khoản đang hoạt động hay bị khoá. Giá trị 1 là hoạt động, 0 là bị vô hiệu hoá |
| 6 | google_id | VARCHAR2(255) | UQ | Mã định danh từ Google OAuth, dùng cho chức năng đăng nhập bằng tài khoản Google |

---

### Bảng 5: ACCOUNT_PERMISSION — Phân quyền riêng cho tài khoản

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | account_id | NUMBER | PK, FK → ACCOUNT | Tham chiếu đến tài khoản được gán quyền riêng |
| 2 | permission_id | NUMBER | PK, FK → PERMISSION | Tham chiếu đến quyền hạn được gán trực tiếp, ngoài quyền mặc định từ vai trò |

> Bảng trung gian cho phép gán quyền bổ sung cho từng tài khoản cụ thể. Khoá chính kép gồm account_id và permission_id.

---

## 2. Phân hệ Cơ sở vật chất & Danh mục chung

---

### Bảng 6: BRANCH — Chi nhánh bệnh viện

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | branch_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của chi nhánh, được hệ thống tự động sinh |
| 2 | branch_name | VARCHAR2(200) | NN, UQ | Tên chi nhánh bệnh viện, không được trùng lặp giữa các chi nhánh |
| 3 | branch_address | VARCHAR2(500) | NN | Địa chỉ đầy đủ của chi nhánh |
| 4 | branch_hotline | VARCHAR2(20) | | Số điện thoại đường dây nóng để bệnh nhân liên hệ |

---

### Bảng 7: ROOM_TYPE — Loại phòng

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | room_type_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của loại phòng, được hệ thống tự động sinh |
| 2 | room_type_name | VARCHAR2(100) | NN, UQ | Tên loại phòng dùng để phân loại mục đích sử dụng, ví dụ Phòng khám, Phòng xét nghiệm, Phòng nội trú |

---

### Bảng 8: ROOM — Phòng

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | room_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của phòng, được hệ thống tự động sinh |
| 2 | room_type_id | NUMBER | NN, FK → ROOM_TYPE | Loại phòng, xác định mục đích sử dụng của phòng |
| 3 | branch_id | NUMBER | NN, FK → BRANCH | Chi nhánh mà phòng này thuộc về |
| 4 | room_code | VARCHAR2(50) | NN, UQ | Mã phòng duy nhất dùng để nhận diện nhanh, ví dụ P-101, XN-203 |
| 5 | position | VARCHAR2(200) | | Vị trí cụ thể của phòng trong toà nhà, ví dụ Tầng 2 - Dãy A |
| 6 | note | VARCHAR2(500) | | Ghi chú bổ sung về tình trạng hoặc đặc điểm của phòng |
| 7 | specialty_id | NUMBER | FK → SPECIALTY | Chuyên khoa mà phòng khám này phục vụ, dùng để mapping phòng khám với chuyên khoa tương ứng |
| 8 | floor | NUMBER | NN, DEFAULT 1 | Số tầng nơi phòng toạ lạc, phục vụ hiển thị sơ đồ tầng trực quan |

---

### Bảng 9: BED — Giường bệnh

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | bed_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của giường bệnh, được hệ thống tự động sinh |
| 2 | room_id | NUMBER | NN, FK → ROOM | Phòng chứa giường bệnh này |
| 3 | price | NUMBER(15,2) | NN, CHECK ≥ 0 | Đơn giá giường mỗi ngày, tính bằng VND |
| 4 | status | VARCHAR2(20) | NN, DEFAULT 'AVAILABLE', CHECK IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') | Trạng thái hiện tại của giường: AVAILABLE là trống sẵn sàng, OCCUPIED là đang có bệnh nhân sử dụng, MAINTENANCE là đang bảo trì |

---

### Bảng 10: SPECIALTY — Chuyên khoa

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | specialty_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của chuyên khoa, được hệ thống tự động sinh |
| 2 | specialty_code | VARCHAR2(50) | NN, UQ | Mã code ngắn gọn của chuyên khoa, ví dụ SPEC-0001, dùng để tra cứu nhanh |
| 3 | specialty_name | VARCHAR2(200) | NN, UQ | Tên đầy đủ của chuyên khoa, ví dụ Nội tổng quát, Tim mạch, Da liễu |
| 4 | is_active | NUMBER(1) | NN, DEFAULT 1, CHECK IN (0, 1) | Cờ đánh dấu chuyên khoa còn hoạt động hay đã ngưng. Giá trị 1 là hoạt động, 0 là ngưng |
| 5 | created_at | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Thời điểm bản ghi chuyên khoa được tạo lần đầu |
| 6 | updated_at | TIMESTAMP | | Thời điểm bản ghi chuyên khoa được cập nhật lần gần nhất |

---

### Bảng 11: CONSULTATION_FEE — Phí khám bệnh

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | fee_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của phí khám, được hệ thống tự động sinh |
| 2 | fee_code | VARCHAR2(50) | NN, UQ | Mã code phí khám dùng để tra cứu và hiển thị trên hoá đơn |
| 3 | fee_name | VARCHAR2(200) | NN | Tên mô tả của loại phí khám, ví dụ Phí khám chuyên khoa Tim mạch |
| 4 | specialty | VARCHAR2(100) | NN, UQ | Tên chuyên khoa dạng text, mỗi chuyên khoa chỉ có một mức phí duy nhất |
| 5 | price | NUMBER(15,2) | NN, CHECK ≥ 0 | Số tiền phí khám, tính bằng VND |
| 6 | is_active | NUMBER(1) | NN, DEFAULT 1, CHECK IN (0, 1) | Cờ đánh dấu phí khám còn áp dụng hay đã ngưng. Giá trị 1 là hoạt động, 0 là ngưng |
| 7 | created_at | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Thời điểm bản ghi được tạo lần đầu |
| 8 | updated_at | TIMESTAMP | | Thời điểm bản ghi được cập nhật lần gần nhất |
| 9 | specialty_id | NUMBER | UQ, FK → SPECIALTY | Liên kết đến bảng SPECIALTY, đảm bảo mỗi chuyên khoa chỉ có một mức phí khám duy nhất |

---

## 3. Phân hệ Nhân sự & Bệnh nhân

---

### Bảng 12: DOCTOR — Bác sĩ

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | doctor_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của bác sĩ, được hệ thống tự động sinh |
| 2 | account_id | NUMBER | NN, UQ, FK → ACCOUNT | Tài khoản đăng nhập tương ứng, mỗi bác sĩ liên kết với đúng một tài khoản |
| 3 | full_name | VARCHAR2(200) | NN | Họ và tên đầy đủ của bác sĩ |
| 4 | qualification | VARCHAR2(200) | | Bằng cấp chuyên môn cao nhất, ví dụ Thạc sĩ Y khoa, Tiến sĩ Y học |
| 5 | specialization | VARCHAR2(200) | | Lĩnh vực chuyên môn dạng text gốc, ví dụ Nội khoa, Ngoại khoa |
| 6 | license_num | VARCHAR2(100) | NN, UQ | Số giấy phép hành nghề y do Bộ Y tế cấp, bắt buộc và không trùng lặp |
| 7 | identity_num | VARCHAR2(50) | UQ | Số CMND hoặc CCCD, dùng để xác minh danh tính |
| 8 | gender | VARCHAR2(10) | CHECK IN ('MALE', 'FEMALE', 'OTHER') | Giới tính của bác sĩ |
| 9 | phone | VARCHAR2(20) | | Số điện thoại liên lạc |
| 10 | address | VARCHAR2(500) | | Địa chỉ thường trú |
| 11 | date_of_birth | DATE | | Ngày tháng năm sinh |
| 12 | hire_date | DATE | | Ngày bắt đầu làm việc tại bệnh viện |
| 13 | experience | VARCHAR2(500) | | Mô tả kinh nghiệm làm việc dạng text tự do. Đã được đổi từ kiểu số sang văn bản tại V13 |
| 14 | is_active | NUMBER(1) | NN, DEFAULT 1, CHECK IN (0, 1) | Cờ đánh dấu bác sĩ đang làm việc hay đã nghỉ. Giá trị 1 là đang làm việc, 0 là đã nghỉ |
| 15 | specialty_id | NUMBER | FK → SPECIALTY | Chuyên khoa chính mà bác sĩ đảm nhận, liên kết đến bảng danh mục chuyên khoa |

---

### Bảng 13: RECEPTIONIST — Lễ tân

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | receptionist_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của lễ tân, được hệ thống tự động sinh |
| 2 | account_id | NUMBER | NN, UQ, FK → ACCOUNT | Tài khoản đăng nhập tương ứng, mỗi lễ tân liên kết với đúng một tài khoản |
| 3 | full_name | VARCHAR2(200) | NN | Họ và tên đầy đủ |
| 4 | identity_num | VARCHAR2(50) | UQ | Số CMND hoặc CCCD, dùng để xác minh danh tính |
| 5 | gender | VARCHAR2(10) | CHECK IN ('MALE', 'FEMALE', 'OTHER') | Giới tính |
| 6 | phone | VARCHAR2(20) | | Số điện thoại liên lạc |
| 7 | address | VARCHAR2(500) | | Địa chỉ thường trú |
| 8 | date_of_birth | DATE | | Ngày tháng năm sinh |
| 9 | hire_date | DATE | | Ngày bắt đầu làm việc tại bệnh viện |
| 10 | shift | VARCHAR2(50) | | Ca làm việc được phân công, ví dụ Morning, Evening, Night |
| 11 | is_active | NUMBER(1) | NN, DEFAULT 1, CHECK IN (0, 1) | Cờ đánh dấu lễ tân đang làm việc hay đã nghỉ. Giá trị 1 là đang làm việc, 0 là đã nghỉ |

---

### Bảng 14: TECHNICIAN — Kỹ thuật viên

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | technician_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của kỹ thuật viên, được hệ thống tự động sinh |
| 2 | account_id | NUMBER | NN, UQ, FK → ACCOUNT | Tài khoản đăng nhập tương ứng, mỗi kỹ thuật viên liên kết với đúng một tài khoản |
| 3 | full_name | VARCHAR2(200) | NN | Họ và tên đầy đủ |
| 4 | qualification | VARCHAR2(200) | | Bằng cấp chuyên môn |
| 5 | specialty_area | VARCHAR2(200) | | Lĩnh vực chuyên trách trong bệnh viện, ví dụ X-Quang, Xét nghiệm, Siêu âm |
| 6 | license_num | VARCHAR2(100) | UQ | Chứng chỉ hành nghề nếu có, không bắt buộc |
| 7 | identity_num | VARCHAR2(50) | UQ | Số CMND hoặc CCCD, dùng để xác minh danh tính |
| 8 | gender | VARCHAR2(10) | CHECK IN ('MALE', 'FEMALE', 'OTHER') | Giới tính |
| 9 | phone | VARCHAR2(20) | | Số điện thoại liên lạc |
| 10 | address | VARCHAR2(500) | | Địa chỉ thường trú |
| 11 | date_of_birth | DATE | | Ngày tháng năm sinh |
| 12 | hire_date | DATE | | Ngày bắt đầu làm việc tại bệnh viện |
| 13 | experience | NUMBER(3) | | Số năm kinh nghiệm làm việc trong lĩnh vực chuyên môn |
| 14 | is_active | NUMBER(1) | NN, DEFAULT 1, CHECK IN (0, 1) | Cờ đánh dấu kỹ thuật viên đang làm việc hay đã nghỉ. Giá trị 1 là đang làm việc, 0 là đã nghỉ |

---

### Bảng 15: PHARMACIST — Dược sĩ

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | pharmacist_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của dược sĩ, được hệ thống tự động sinh |
| 2 | account_id | NUMBER | NN, UQ, FK → ACCOUNT | Tài khoản đăng nhập tương ứng, mỗi dược sĩ liên kết với đúng một tài khoản |
| 3 | full_name | VARCHAR2(200) | NN | Họ và tên đầy đủ |
| 4 | qualification | VARCHAR2(200) | | Bằng cấp chuyên môn về dược |
| 5 | license_num | VARCHAR2(100) | NN, UQ | Chứng chỉ hành nghề Dược do cơ quan có thẩm quyền cấp, bắt buộc phải có |
| 6 | identity_num | VARCHAR2(50) | UQ | Số CMND hoặc CCCD, dùng để xác minh danh tính |
| 7 | gender | VARCHAR2(10) | CHECK IN ('MALE', 'FEMALE', 'OTHER') | Giới tính |
| 8 | phone | VARCHAR2(20) | | Số điện thoại liên lạc |
| 9 | address | VARCHAR2(500) | | Địa chỉ thường trú |
| 10 | date_of_birth | DATE | | Ngày tháng năm sinh |
| 11 | hire_date | DATE | | Ngày bắt đầu làm việc tại bệnh viện |
| 12 | experience | NUMBER(3) | | Số năm kinh nghiệm làm việc trong lĩnh vực dược |
| 13 | is_active | NUMBER(1) | NN, DEFAULT 1, CHECK IN (0, 1) | Cờ đánh dấu dược sĩ đang làm việc hay đã nghỉ. Giá trị 1 là đang làm việc, 0 là đã nghỉ |

---

### Bảng 16: ACCOUNTANT — Kế toán

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | accountant_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của kế toán, được hệ thống tự động sinh |
| 2 | account_id | NUMBER | NN, UQ, FK → ACCOUNT | Tài khoản đăng nhập tương ứng, mỗi kế toán liên kết với đúng một tài khoản |
| 3 | full_name | VARCHAR2(200) | NN | Họ và tên đầy đủ |
| 4 | qualification | VARCHAR2(200) | | Bằng cấp và chứng chỉ chuyên môn, ví dụ CPA, ACCA |
| 5 | identity_num | VARCHAR2(50) | UQ | Số CMND hoặc CCCD, dùng để xác minh danh tính |
| 6 | gender | VARCHAR2(10) | CHECK IN ('MALE', 'FEMALE', 'OTHER') | Giới tính |
| 7 | phone | VARCHAR2(20) | | Số điện thoại liên lạc |
| 8 | address | VARCHAR2(500) | | Địa chỉ thường trú |
| 9 | date_of_birth | DATE | | Ngày tháng năm sinh |
| 10 | hire_date | DATE | | Ngày bắt đầu làm việc tại bệnh viện |
| 11 | experience | NUMBER(3) | | Số năm kinh nghiệm làm việc trong lĩnh vực kế toán |
| 12 | is_active | NUMBER(1) | NN, DEFAULT 1, CHECK IN (0, 1) | Cờ đánh dấu kế toán đang làm việc hay đã nghỉ. Giá trị 1 là đang làm việc, 0 là đã nghỉ |

---

### Bảng 17: ADMINISTRATOR — Quản trị viên

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | administrator_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của quản trị viên, được hệ thống tự động sinh |
| 2 | account_id | NUMBER | NN, UQ, FK → ACCOUNT | Tài khoản đăng nhập tương ứng, mỗi quản trị viên liên kết với đúng một tài khoản |
| 3 | full_name | VARCHAR2(200) | NN | Họ và tên đầy đủ |
| 4 | identity_num | VARCHAR2(50) | UQ | Số CMND hoặc CCCD, dùng để xác minh danh tính |
| 5 | gender | VARCHAR2(10) | CHECK IN ('MALE', 'FEMALE', 'OTHER') | Giới tính |
| 6 | phone | VARCHAR2(20) | | Số điện thoại liên lạc |
| 7 | address | VARCHAR2(500) | | Địa chỉ thường trú |
| 8 | date_of_birth | DATE | | Ngày tháng năm sinh |
| 9 | hire_date | DATE | | Ngày bắt đầu làm việc tại bệnh viện |
| 10 | is_active | NUMBER(1) | NN, DEFAULT 1, CHECK IN (0, 1) | Cờ đánh dấu quản trị viên đang làm việc hay đã nghỉ. Giá trị 1 là đang làm việc, 0 là đã nghỉ |

---

### Bảng 18: PATIENT — Bệnh nhân

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | patient_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của bệnh nhân, được hệ thống tự động sinh |
| 2 | account_id | NUMBER | UQ, FK → ACCOUNT | Tài khoản đăng nhập nếu bệnh nhân đăng ký online. Cho phép null vì bệnh nhân đến trực tiếp có thể chưa có tài khoản |
| 3 | full_name | VARCHAR2(200) | NN | Họ và tên đầy đủ của bệnh nhân |
| 4 | gender | VARCHAR2(10) | CHECK IN ('MALE', 'FEMALE', 'OTHER') | Giới tính |
| 5 | date_of_birth | DATE | | Ngày tháng năm sinh |
| 6 | phone | VARCHAR2(20) | | Số điện thoại liên lạc |
| 7 | address | VARCHAR2(500) | | Địa chỉ thường trú |
| 8 | identity_num | VARCHAR2(50) | UQ | Số CMND hoặc CCCD, dùng để xác minh danh tính bệnh nhân |
| 9 | medical_history | CLOB | | Tiền sử bệnh lý của bệnh nhân, lưu dạng văn bản dài không giới hạn |
| 10 | allergy | VARCHAR2(1000) | | Thông tin dị ứng thuốc hoặc thực phẩm cần lưu ý khi khám và kê đơn |
| 11 | is_active | NUMBER(1) | NN, DEFAULT 1, CHECK IN (0, 1) | Cờ đánh dấu hồ sơ bệnh nhân còn hoạt động hay đã ngưng. Giá trị 1 là hoạt động, 0 là ngưng |

---

### Bảng 19: PATIENT_INSURANCE — Bảo hiểm y tế bệnh nhân

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | patient_insurance_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của bản ghi bảo hiểm, được hệ thống tự động sinh |
| 2 | patient_id | NUMBER | NN, FK → PATIENT | Bệnh nhân sở hữu thẻ bảo hiểm này |
| 3 | insurance_num | VARCHAR2(100) | NN, UQ | Số thẻ bảo hiểm y tế, mỗi thẻ là duy nhất trong hệ thống |
| 4 | status | VARCHAR2(20) | NN, DEFAULT 'ACTIVE', CHECK IN ('ACTIVE', 'EXPIRED', 'SUSPENDED') | Trạng thái hiện tại của thẻ bảo hiểm: ACTIVE là còn hiệu lực, EXPIRED là hết hạn, SUSPENDED là tạm ngưng |
| 5 | expiry_date | DATE | NN | Ngày hết hạn của thẻ bảo hiểm |
| 6 | coverage_percent | NUMBER(5,2) | NN, CHECK BETWEEN 0 AND 100 | Tỷ lệ phần trăm chi phí được bảo hiểm chi trả, giá trị từ 0 đến 100 |

---

## 4. Phân hệ Lịch làm việc & Đặt hẹn

---

### Bảng 20: DOCTOR_SCHEDULE — Lịch trực bác sĩ

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | doctor_schedule_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của lịch trực, được hệ thống tự động sinh |
| 2 | doctor_id | NUMBER | NN, FK → DOCTOR | Bác sĩ được phân công trực trong ca này |
| 3 | room_id | NUMBER | NN, FK → ROOM | Phòng khám nơi bác sĩ ngồi trực |
| 4 | schedule_date | DATE | NN | Ngày trực cụ thể |
| 5 | shift | VARCHAR2(20) | NN, CHECK IN ('MORNING', 'AFTERNOON') | Ca trực trong ngày: MORNING là ca sáng, AFTERNOON là ca chiều. Đã thay thế cặp cột start_time và end_time từ V3 |
| 6 | max_capacity | NUMBER(5) | NN, CHECK > 0 | Số lượng bệnh nhân tối đa mà bác sĩ có thể tiếp nhận trong ca trực |
| 7 | current_booking_count | NUMBER(5) | NN, DEFAULT 0, CHECK ≥ 0 | Số lượng lịch hẹn đã được đặt trong ca trực này, tự động cập nhật khi có lịch hẹn mới |
| 8 | last_queue_number | NUMBER(5) | NN, DEFAULT 0, CHECK ≥ 0 | Số thứ tự cuối cùng đã được cấp cho bệnh nhân, dùng để sinh số thứ tự mới |
| 9 | note | VARCHAR2(500) | | Ghi chú bổ sung về ca trực, ví dụ thông báo nghỉ bù hoặc thay đổi lịch |
| 10 | created_at | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Thời điểm bản ghi lịch trực được tạo lần đầu |
| 11 | updated_at | TIMESTAMP | | Thời điểm bản ghi lịch trực được cập nhật lần gần nhất |
| 12 | version_number | NUMBER(19) | NN, DEFAULT 0 | Số phiên bản dùng cho cơ chế Optimistic Locking, ngăn chặn xung đột khi nhiều người cùng cập nhật |

> Ràng buộc duy nhất: một bác sĩ chỉ có một lịch trực trong cùng ngày và ca. Một phòng chỉ có một bác sĩ trực trong cùng ngày và ca.

---

### Bảng 21: APPOINTMENT — Lịch hẹn khám bệnh

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | appointment_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của lịch hẹn, được hệ thống tự động sinh |
| 2 | patient_id | NUMBER | NN, FK → PATIENT | Bệnh nhân đặt lịch hẹn khám |
| 3 | doctor_schedule_id | NUMBER | NN, FK → DOCTOR_SCHEDULE | Ca trực bác sĩ mà lịch hẹn này thuộc về |
| 4 | queue_num | NUMBER(5) | | Số thứ tự hàng đợi khám bệnh. Cho phép null vì lúc đặt hẹn online chưa có số thứ tự, chỉ được cấp khi check-in |
| 5 | status | VARCHAR2(20) | NN, DEFAULT 'PENDING', CHECK IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'PAYMENT_EXPIRED') | Trạng thái vòng đời của lịch hẹn: PENDING là chờ xác nhận, CONFIRMED là đã xác nhận, CHECKED_IN là đã đến quầy, IN_PROGRESS là đang khám, COMPLETED là hoàn tất, CANCELLED là đã huỷ, PAYMENT_EXPIRED là hết hạn thanh toán |
| 6 | created_at | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Thời điểm lịch hẹn được tạo |
| 7 | initial_symptoms | CLOB | | Triệu chứng ban đầu do bệnh nhân tự mô tả khi đặt hẹn |
| 8 | visit_reason | VARCHAR2(500) | | Lý do khám bệnh, ví dụ tái khám, khám định kỳ, khám mới |
| 9 | updated_at | TIMESTAMP | | Thời điểm lịch hẹn được cập nhật lần gần nhất |
| 10 | checked_in_at | TIMESTAMP | | Thời điểm bệnh nhân thực hiện check-in tại quầy lễ tân |
| 11 | cancelled_at | TIMESTAMP | | Thời điểm lịch hẹn bị huỷ bỏ |
| 12 | paid_at | TIMESTAMP | | Thời điểm bệnh nhân hoàn tất thanh toán phí khám |
| 13 | version_number | NUMBER(19) | NN, DEFAULT 0 | Số phiên bản dùng cho cơ chế Optimistic Locking, ngăn chặn xung đột khi nhiều người cùng cập nhật |
| 14 | appointment_code | VARCHAR2(30) | NN, UQ | Mã lịch hẹn duy nhất để tra cứu và hiển thị cho bệnh nhân, ví dụ APT-00000123 |
| 15 | sepay_transaction_id | NUMBER(19) | UQ | Mã giao dịch trả về từ cổng thanh toán SePay, dùng để đối soát webhook |
| 16 | payment_reference_code | VARCHAR2(200) | | Mã tham chiếu thanh toán, dùng để khớp giao dịch chuyển khoản từ ngân hàng |
| 17 | payment_content | VARCHAR2(1000) | | Nội dung chuyển khoản mà bệnh nhân cần ghi khi thanh toán online |
| 18 | fee_id | NUMBER | FK → CONSULTATION_FEE | Mức phí khám được áp dụng cho lịch hẹn này |
| 19 | fee_name_snapshot | VARCHAR2(200) | | Bản sao tên phí khám tại thời điểm đặt hẹn, tránh ảnh hưởng khi phí khám thay đổi sau |
| 20 | fee_price_snapshot | NUMBER(15,2) | | Bản sao giá phí khám tại thời điểm đặt hẹn, tránh ảnh hưởng khi phí khám thay đổi sau |
| 21 | payment_expires_at | TIMESTAMP | | Thời hạn thanh toán phí khám online, quá hạn này lịch hẹn sẽ tự động bị huỷ |

> Ràng buộc duy nhất: mỗi ca trực không có hai bệnh nhân trùng số thứ tự.

---

## 5. Phân hệ Khám bệnh & Nội trú

---

### Bảng 22: MEDICAL_RECORD — Hồ sơ bệnh án

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | med_record_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của bệnh án, được hệ thống tự động sinh |
| 2 | appointment_id | NUMBER | NN, UQ, FK → APPOINTMENT | Lịch hẹn khám đã sinh ra bệnh án này, quan hệ một-một |
| 3 | record_date | DATE | | Ngày ghi nhận khám bệnh. Cho phép null khi bệnh án đang ở trạng thái nháp |
| 4 | initial_diagnosis | VARCHAR2(1000) | | Chẩn đoán sơ bộ ban đầu của bác sĩ khi bắt đầu khám |
| 5 | clinical_conclusion | CLOB | | Kết luận lâm sàng cuối cùng của bác sĩ sau khi khám và có đủ kết quả xét nghiệm |
| 6 | total_price | NUMBER(15,2) | DEFAULT 0, CHECK ≥ 0 | Tổng chi phí các dịch vụ và xét nghiệm liên quan đến bệnh án, tính bằng VND |
| 7 | doctor_id | NUMBER | NN, FK → DOCTOR | Bác sĩ phụ trách khám và ghi bệnh án |
| 8 | patient_id | NUMBER | NN, FK → PATIENT | Bệnh nhân được khám trong bệnh án này |
| 9 | conclusion_type | VARCHAR2(30) | CHECK IN ('COMPLETED', 'ADMISSION_REQUIRED') | Loại kết luận: COMPLETED là hoàn tất khám ngoại trú, ADMISSION_REQUIRED là cần nhập viện nội trú. Cho phép null khi bệnh án chưa hoàn thành |
| 10 | clinical_notes | CLOB | | Ghi chú lâm sàng chi tiết của bác sĩ trong quá trình khám |
| 11 | treatment_plan | CLOB | | Phác đồ điều trị mà bác sĩ đề ra cho bệnh nhân |
| 12 | status | VARCHAR2(20) | NN, CHECK IN ('DRAFT', 'IN_PROGRESS', 'COMPLETED', 'LOCKED') | Trạng thái vòng đời của bệnh án: DRAFT là nháp mới tạo, IN_PROGRESS là đang khám, COMPLETED là đã hoàn thành, LOCKED là đã khoá không cho chỉnh sửa |
| 13 | created_at | TIMESTAMP | NN | Thời điểm bệnh án được tạo lần đầu |
| 14 | updated_at | TIMESTAMP | | Thời điểm bệnh án được cập nhật lần gần nhất |
| 15 | completed_at | TIMESTAMP | | Thời điểm bác sĩ đánh dấu hoàn thành bệnh án |
| 16 | locked_at | TIMESTAMP | | Thời điểm bệnh án bị khoá, sau khi khoá không thể chỉnh sửa |
| 17 | version_number | NUMBER(19) | NN, DEFAULT 0 | Số phiên bản dùng cho cơ chế Optimistic Locking, ngăn chặn xung đột khi nhiều người cùng cập nhật |

---

### Bảng 23: ADMISSION_REQUEST — Yêu cầu nhập viện nội trú

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | admission_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của yêu cầu nhập viện, được hệ thống tự động sinh |
| 2 | patient_id | NUMBER | NN, FK → PATIENT | Bệnh nhân cần nhập viện |
| 3 | med_record_id | NUMBER | NN, UQ, FK → MEDICAL_RECORD | Bệnh án chỉ định nhập viện, quan hệ một-một |
| 4 | bed_id | NUMBER | NN, FK → BED | Giường bệnh được chỉ định cho bệnh nhân |
| 5 | admission_date | DATE | NN | Ngày bệnh nhân nhập viện |
| 6 | discharge_date | DATE | CHECK ≥ admission_date hoặc NULL | Ngày bệnh nhân xuất viện. Để trống khi bệnh nhân còn đang nằm viện |
| 7 | status | VARCHAR2(20) | NN, DEFAULT 'PENDING', CHECK IN ('PENDING', 'ADMITTED', 'DISCHARGED', 'CANCELLED') | Trạng thái yêu cầu: PENDING là chờ duyệt, ADMITTED là đã nhập viện, DISCHARGED là đã xuất viện, CANCELLED là đã huỷ |
| 8 | total_price | NUMBER(15,2) | DEFAULT 0, CHECK ≥ 0 | Tổng chi phí nội trú bao gồm tiền giường và các phí phát sinh |

---

### Bảng 24: ADMISSION_RECORD — Theo dõi sinh hiệu nội trú

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | admission_record_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của bản ghi sinh hiệu, được hệ thống tự động sinh |
| 2 | admission_id | NUMBER | NN, FK → ADMISSION_REQUEST | Yêu cầu nhập viện mà bản ghi sinh hiệu này thuộc về |
| 3 | blood_pressure | VARCHAR2(20) | | Chỉ số huyết áp đo được, ví dụ 120/80 mmHg |
| 4 | heart_rate | NUMBER(5) | | Nhịp tim đo được, đơn vị lần trên phút |
| 5 | temperature | NUMBER(5,2) | | Nhiệt độ cơ thể đo được, đơn vị độ C |
| 6 | record_date | DATE | NN | Ngày thực hiện đo sinh hiệu |

---

## 6. Phân hệ Xét nghiệm

---

### Bảng 25: LAB_TEST — Danh mục xét nghiệm

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | lab_test_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của loại xét nghiệm, được hệ thống tự động sinh |
| 2 | lab_test_name | VARCHAR2(200) | NN, UQ | Tên loại xét nghiệm, ví dụ Công thức máu, Đường huyết, HIV |
| 3 | price | NUMBER(15,2) | NN, CHECK ≥ 0 | Đơn giá xét nghiệm hiện hành, tính bằng VND |
| 4 | is_active | NUMBER(1) | NN, DEFAULT 1, CHECK IN (0, 1) | Cờ đánh dấu loại xét nghiệm còn được cung cấp hay đã ngưng. Giá trị 1 là hoạt động, 0 là ngưng |

---

### Bảng 26: LAB_TEST_REQUEST — Phiếu yêu cầu xét nghiệm

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | lab_test_request_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của phiếu xét nghiệm, được hệ thống tự động sinh |
| 2 | med_record_id | NUMBER | NN, FK → MEDICAL_RECORD | Bệnh án chỉ định thực hiện xét nghiệm |
| 3 | request_code | VARCHAR2(100) | NN, UQ | Mã phiếu yêu cầu duy nhất để tra cứu và in phiếu |
| 4 | status | VARCHAR2(20) | NN, CHECK IN ('NOT_COLLECTED', 'SAMPLE_COLLECTED', 'RESULT_AVAILABLE') | Trạng thái quy trình xét nghiệm: NOT_COLLECTED là chưa lấy mẫu, SAMPLE_COLLECTED là đã lấy mẫu, RESULT_AVAILABLE là đã có kết quả |
| 5 | payment_status | VARCHAR2(20) | NN, DEFAULT 'UNPAID', CHECK IN ('UNPAID', 'PAID') | Trạng thái thanh toán: UNPAID là chưa thanh toán, PAID là đã thanh toán |
| 6 | total_price | NUMBER(15,2) | DEFAULT 0, CHECK ≥ 0 | Tổng chi phí tất cả các mục xét nghiệm trong phiếu, tính bằng VND |
| 7 | note | VARCHAR2(500) | | Ghi chú của bác sĩ kèm theo phiếu xét nghiệm, ví dụ yêu cầu nhịn ăn trước khi lấy mẫu |
| 8 | created_at | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Thời điểm phiếu xét nghiệm được tạo |
| 9 | updated_at | TIMESTAMP | | Thời điểm phiếu được cập nhật lần gần nhất |
| 10 | paid_at | TIMESTAMP | | Thời điểm bệnh nhân thanh toán phí xét nghiệm |

---

### Bảng 27: LAB_TEST_REQUEST_ITEM — Chi tiết phiếu xét nghiệm

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | lab_test_request_id | NUMBER | PK, FK → LAB_TEST_REQUEST | Phiếu yêu cầu xét nghiệm chứa mục này |
| 2 | lab_test_id | NUMBER | PK, FK → LAB_TEST | Loại xét nghiệm được chỉ định |
| 3 | snapshot_price | NUMBER(15,2) | NN, CHECK ≥ 0 | Giá xét nghiệm được ghi lại tại thời điểm chỉ định, đảm bảo không bị ảnh hưởng khi bảng giá thay đổi |

> Khoá chính kép gồm lab_test_request_id và lab_test_id. Mỗi phiếu xét nghiệm có thể chứa nhiều mục, mỗi loại xét nghiệm chỉ xuất hiện một lần trong cùng phiếu.

---

### Bảng 28: LAB_TEST_RESULT — Kết quả xét nghiệm

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | lab_test_result_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của kết quả xét nghiệm, được hệ thống tự động sinh |
| 2 | lab_test_request_id | NUMBER | NN, UQ, FK → LAB_TEST_REQUEST | Phiếu xét nghiệm tương ứng, quan hệ một-một vì mỗi phiếu chỉ có một bản kết quả |
| 3 | result_data | CLOB | | Dữ liệu kết quả xét nghiệm dạng văn bản hoặc JSON, lưu trữ chỉ số và giá trị đo được |
| 4 | result_date | DATE | NN | Ngày kỹ thuật viên trả kết quả xét nghiệm |

---

## 7. Phân hệ Dịch vụ Chức năng

---

### Bảng 29: MEDICAL_SERVICE — Danh mục dịch vụ y tế

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | med_service_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của dịch vụ, được hệ thống tự động sinh |
| 2 | medical_service_name | VARCHAR2(200) | NN, UQ | Tên dịch vụ y tế, ví dụ Siêu âm ổ bụng, Chụp X-Quang ngực, Điện tâm đồ |
| 3 | price | NUMBER(15,2) | NN, CHECK ≥ 0 | Đơn giá dịch vụ hiện hành, tính bằng VND |
| 4 | is_active | NUMBER(1) | NN, DEFAULT 1, CHECK IN (0, 1) | Cờ đánh dấu dịch vụ còn được cung cấp hay đã ngưng. Giá trị 1 là hoạt động, 0 là ngưng |

---

### Bảng 30: MEDICAL_SERVICE_REQUEST — Phiếu yêu cầu dịch vụ

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | med_ser_req_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của phiếu dịch vụ, được hệ thống tự động sinh |
| 2 | med_record_id | NUMBER | NN, FK → MEDICAL_RECORD | Bệnh án chỉ định thực hiện dịch vụ |
| 3 | request_code | VARCHAR2(100) | NN, UQ | Mã phiếu yêu cầu duy nhất để tra cứu và in phiếu |
| 4 | status | VARCHAR2(20) | NN, CHECK IN ('NOT_COLLECTED', 'SAMPLE_COLLECTED', 'RESULT_AVAILABLE') | Trạng thái quy trình thực hiện dịch vụ: NOT_COLLECTED là chưa tiếp nhận, SAMPLE_COLLECTED là đã tiếp nhận bệnh nhân, RESULT_AVAILABLE là đã có kết quả |
| 5 | payment_status | VARCHAR2(20) | NN, DEFAULT 'UNPAID', CHECK IN ('UNPAID', 'PAID') | Trạng thái thanh toán: UNPAID là chưa thanh toán, PAID là đã thanh toán |
| 6 | total_price | NUMBER(15,2) | DEFAULT 0, CHECK ≥ 0 | Tổng chi phí tất cả các mục dịch vụ trong phiếu, tính bằng VND |
| 7 | currency | VARCHAR2(10) | DEFAULT 'VND' | Đơn vị tiền tệ dùng để tính chi phí |
| 8 | note | VARCHAR2(500) | | Ghi chú của bác sĩ kèm theo phiếu dịch vụ |
| 9 | created_at | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Thời điểm phiếu dịch vụ được tạo |
| 10 | updated_at | TIMESTAMP | | Thời điểm phiếu được cập nhật lần gần nhất |
| 11 | confirmed_at | TIMESTAMP | | Thời điểm kỹ thuật viên xác nhận tiếp nhận phiếu dịch vụ |
| 12 | cancelled_at | TIMESTAMP | | Thời điểm phiếu dịch vụ bị huỷ bỏ |
| 13 | paid_at | TIMESTAMP | | Thời điểm bệnh nhân thanh toán phí dịch vụ |

---

### Bảng 31: MEDICAL_SERVICE_REQUEST_ITEM — Chi tiết phiếu dịch vụ

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | med_ser_req_id | NUMBER | PK, FK → MEDICAL_SERVICE_REQUEST | Phiếu yêu cầu dịch vụ chứa mục này |
| 2 | med_service_id | NUMBER | PK, FK → MEDICAL_SERVICE | Loại dịch vụ được chỉ định |
| 3 | snapshot_price | NUMBER(15,2) | NN, CHECK ≥ 0 | Giá dịch vụ được ghi lại tại thời điểm chỉ định, đảm bảo không bị ảnh hưởng khi bảng giá thay đổi |

> Khoá chính kép gồm med_ser_req_id và med_service_id. Mỗi phiếu dịch vụ có thể chứa nhiều mục, mỗi loại dịch vụ chỉ xuất hiện một lần trong cùng phiếu.

---

### Bảng 32: MEDICAL_SERVICE_RESULT — Kết quả dịch vụ

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | med_service_result_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của kết quả dịch vụ, được hệ thống tự động sinh |
| 2 | med_ser_req_id | NUMBER | NN, UQ, FK → MEDICAL_SERVICE_REQUEST | Phiếu dịch vụ tương ứng, quan hệ một-một vì mỗi phiếu chỉ có một bản kết quả |
| 3 | result_data | CLOB | | Dữ liệu kết quả dịch vụ dạng văn bản hoặc JSON, ví dụ mô tả hình ảnh siêu âm hoặc nhận xét X-Quang |
| 4 | created_at | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Thời điểm kết quả dịch vụ được ghi nhận |

---

## 8. Phân hệ Dược & Kê đơn

---

### Bảng 33: MEDICINE — Danh mục thuốc

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | medicine_id | NUMBER | PK, IDENTITY BY DEFAULT | Mã định danh duy nhất của thuốc, được hệ thống tự động sinh |
| 2 | medicine_name | VARCHAR2(200) | NN, UQ | Tên thuốc thương mại, ví dụ Paracetamol 500mg, Amoxicillin 250mg |
| 3 | active_ingredient | VARCHAR2(200) | | Hoạt chất chính của thuốc, dùng để tra cứu và kiểm tra tương tác thuốc |
| 4 | unit | VARCHAR2(50) | NN | Đơn vị tính nhỏ nhất của thuốc, ví dụ viên, ống, chai, gói |
| 5 | description | VARCHAR2(500) | | Mô tả bổ sung về công dụng, chỉ định và chống chỉ định của thuốc |
| 6 | is_active | NUMBER(1) | NN, DEFAULT 1 | Cờ đánh dấu thuốc còn được phép kê đơn hay đã ngưng sử dụng. Giá trị 1 là hoạt động, 0 là ngưng |

> Bảng đã được xoá và tạo lại hoàn toàn tại V24 với cấu trúc mới.

---

### Bảng 34: MEDICINE_LOT — Lô thuốc nhập kho

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | medicine_lot_id | NUMBER | PK, IDENTITY BY DEFAULT | Mã định danh duy nhất của lô thuốc, được hệ thống tự động sinh |
| 2 | medicine_id | NUMBER | NN, FK → MEDICINE | Thuốc thuộc lô này |
| 3 | lot_number | VARCHAR2(100) | NN | Số lô do nhà sản xuất in trên bao bì, dùng để truy xuất nguồn gốc |
| 4 | manufacturing_date | DATE | | Ngày sản xuất của lô thuốc |
| 5 | expiry_date | DATE | NN, CHECK > manufacturing_date nếu có | Ngày hết hạn sử dụng, bắt buộc phải sau ngày sản xuất |
| 6 | quantity | NUMBER | NN, DEFAULT 0, CHECK ≥ 0 | Số lượng thuốc còn tồn trong kho của lô này |
| 7 | import_price | NUMBER(15,2) | CHECK ≥ 0 hoặc NULL | Giá nhập kho mỗi đơn vị thuốc, tính bằng VND |
| 8 | is_active | NUMBER(1) | NN, DEFAULT 1 | Cờ đánh dấu lô thuốc còn được phép xuất kho hay đã ngưng. Giá trị 1 là hoạt động, 0 là ngưng |

> Bảng đã được xoá và tạo lại hoàn toàn tại V25. Ràng buộc duy nhất: mỗi thuốc không có hai lô trùng số lô.

---

### Bảng 35: PRESCRIPTION — Đơn thuốc

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | prescription_id | NUMBER | PK, IDENTITY BY DEFAULT | Mã định danh duy nhất của đơn thuốc, được hệ thống tự động sinh |
| 2 | med_record_id | NUMBER | NN, UQ, FK → MEDICAL_RECORD | Bệnh án sinh ra đơn thuốc này, quan hệ một-một vì mỗi bệnh án chỉ có một đơn thuốc |
| 3 | note | CLOB | | Ghi chú và dặn dò chung của bác sĩ dành cho bệnh nhân về việc sử dụng thuốc |
| 4 | is_active | NUMBER(1) | NN, DEFAULT 1 | Cờ đánh dấu đơn thuốc còn hiệu lực hay đã bị huỷ. Giá trị 1 là hoạt động, 0 là ngưng |
| 5 | created_at | TIMESTAMP | NN, DEFAULT CURRENT_TIMESTAMP | Thời điểm đơn thuốc được tạo |
| 6 | updated_at | TIMESTAMP | | Thời điểm đơn thuốc được cập nhật lần gần nhất |

> Bảng đã được xoá và tạo lại hoàn toàn tại V26 với cấu trúc mới.

---

### Bảng 36: PRESCRIPTION_DETAIL — Chi tiết đơn thuốc

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | prescription_detail_id | NUMBER | PK, IDENTITY BY DEFAULT | Mã định danh duy nhất của dòng chi tiết đơn thuốc, được hệ thống tự động sinh |
| 2 | prescription_id | NUMBER | NN, FK → PRESCRIPTION | Đơn thuốc chứa dòng chi tiết này |
| 3 | medicine_id | NUMBER | NN, FK → MEDICINE | Thuốc được kê trong dòng chi tiết này |
| 4 | dosage | VARCHAR2(100) | NN | Liều dùng mỗi lần, ví dụ 1 viên, 5ml, 2 gói |
| 5 | frequency | VARCHAR2(100) | NN | Tần suất sử dụng thuốc, ví dụ 3 lần mỗi ngày, sáng - chiều - tối |
| 6 | duration | VARCHAR2(100) | NN | Thời gian sử dụng thuốc, ví dụ 7 ngày, 2 tuần, 1 tháng |
| 7 | quantity | NUMBER | NN, CHECK ≥ 1 | Tổng số lượng thuốc được kê cho bệnh nhân, tối thiểu 1 |
| 8 | instruction | VARCHAR2(500) | | Hướng dẫn sử dụng bổ sung, ví dụ uống sau ăn, tránh ánh nắng |

> Bảng đã được xoá và tạo lại hoàn toàn tại V26. Khoá chính đơn thay vì khoá chính kép như phiên bản cũ.

---

## 9. Phân hệ Thanh toán

---

### Bảng 37: PAYMENT_RECORD — Hoá đơn thanh toán

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | payment_record_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của hoá đơn, được hệ thống tự động sinh |
| 2 | med_record_id | NUMBER | UQ, FK → MEDICAL_RECORD | Bệnh án liên quan. Cho phép null vì hoá đơn có thể thuộc về lịch hẹn thay vì bệnh án |
| 3 | request_code | VARCHAR2(100) | NN, UQ | Mã hoá đơn duy nhất dùng để tra cứu và in hoá đơn |
| 4 | total_price | NUMBER(15,2) | NN, CHECK ≥ 0 | Tổng số tiền cần thanh toán, tính bằng VND |
| 5 | payment_status | VARCHAR2(20) | NN, DEFAULT 'UNPAID', CHECK IN ('UNPAID', 'PARTIAL', 'PAID') | Trạng thái thanh toán: UNPAID là chưa thanh toán, PARTIAL là đã thanh toán một phần, PAID là đã thanh toán đầy đủ |
| 6 | created_at | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Thời điểm hoá đơn được tạo |
| 7 | updated_at | TIMESTAMP | | Thời điểm hoá đơn được cập nhật lần gần nhất |
| 8 | paid_at | TIMESTAMP | | Thời điểm hoá đơn được thanh toán đầy đủ |
| 9 | appointment_id | NUMBER | UQ, FK → APPOINTMENT | Lịch hẹn liên quan, dùng cho trường hợp thanh toán phí khám. Cho phép null vì hoá đơn có thể thuộc về bệnh án |
| 10 | received_amount | NUMBER(15,2) | NN, DEFAULT 0, CHECK ≥ 0 | Tổng số tiền đã nhận được từ bệnh nhân, dùng để xác định trạng thái thanh toán |

> Ràng buộc logic: mỗi hoá đơn phải thuộc về đúng một trong hai — hoặc bệnh án hoặc lịch hẹn, không được trống cả hai và không được có cả hai cùng lúc.

---

### Bảng 38: PAYMENT_TRANSACTION — Giao dịch thanh toán

| STT | Tên cột | Kiểu dữ liệu | Ràng buộc | Ghi chú |
|-----|---------|---------------|-----------|---------|
| 1 | transaction_id | NUMBER | PK, IDENTITY | Mã định danh duy nhất của giao dịch, được hệ thống tự động sinh |
| 2 | payment_record_id | NUMBER | NN, FK → PAYMENT_RECORD | Hoá đơn mà giao dịch này thanh toán cho |
| 3 | transfer_type | VARCHAR2(50) | | Hình thức chuyển khoản, ví dụ chuyển khoản nội bộ, chuyển khoản liên ngân hàng |
| 4 | gateway | VARCHAR2(50) | NN | Cổng thanh toán xử lý giao dịch, ví dụ SEPAY, CASH, MOMO |
| 5 | account_number | VARCHAR2(100) | | Số tài khoản ngân hàng của người chuyển tiền |
| 6 | sepay_transaction_id | VARCHAR2(200) | UQ | Mã giao dịch do cổng SePay trả về qua webhook, dùng để đối soát tự động |
| 7 | transfer_amount | NUMBER(15,2) | NN, CHECK > 0 | Số tiền thực tế được chuyển trong giao dịch này, phải lớn hơn 0 |
| 8 | transaction_date | TIMESTAMP | NN, DEFAULT SYSTIMESTAMP | Thời điểm giao dịch được ghi nhận |
| 9 | reference_code | VARCHAR2(200) | | Mã tham chiếu từ ngân hàng hoặc cổng thanh toán, dùng để đối chiếu |
| 10 | content | VARCHAR2(500) | | Nội dung chuyển khoản mà người gửi ghi khi thực hiện giao dịch |
| 11 | description | VARCHAR2(1000) | | Mô tả chi tiết về mục đích hoặc bối cảnh của giao dịch |
| 12 | process_status | VARCHAR2(20) | NN, DEFAULT 'PENDING', CHECK IN ('PENDING', 'SUCCESS', 'FAILED') | Trạng thái xử lý giao dịch: PENDING là đang chờ xử lý, SUCCESS là thành công, FAILED là thất bại |
| 13 | raw_data | CLOB | | Dữ liệu thô gốc nhận được từ webhook của cổng thanh toán, lưu nguyên bản để phục vụ đối soát và debug |
| 14 | receipt_number | VARCHAR2(100) | | Số biên lai giấy phát cho bệnh nhân khi thanh toán tại quầy |
| 15 | confirmed_by_account_id | NUMBER | FK → ACCOUNT | Tài khoản nhân viên đã xác nhận giao dịch thanh toán thành công, dùng cho trường hợp thanh toán tại quầy |
