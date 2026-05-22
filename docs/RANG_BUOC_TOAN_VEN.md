# Tài Liệu Tổng Hợp Và Phân Tích Các Ràng Buộc Toàn Vẹn (Integrity Constraints Catalog)

Tài liệu này tổng hợp, phân loại và phân tích chi tiết các **Ràng buộc toàn vẹn (RBTV - Integrity Constraints)** hiện có và đang được áp dụng trong cơ sở dữ liệu của dự án **Hệ thống Quản lý Y tế (Healthcare Management System)**.

Hệ thống ràng buộc toàn vẹn được chia làm 2 nhóm chính theo đúng yêu cầu nghiệp vụ và lý thuyết cơ sở dữ liệu:
1. **Ràng buộc toàn vẹn có bối cảnh trên một quan hệ**: Bao gồm ràng buộc miền giá trị, ràng buộc liên thuộc tính, và ràng buộc liên bộ.
2. **Ràng buộc toàn vẹn có bối cảnh trên nhiều quan hệ**: Bao gồm ràng buộc khóa ngoại, ràng buộc liên thuộc tính - liên quan hệ, và ràng buộc liên bộ - liên quan hệ.

Mỗi mục con dưới đây được minh họa bằng **3 ràng buộc thực tế** từ cấu trúc bảng của hệ thống, bao gồm đầy đủ: **Bối cảnh**, **Nội dung (biểu diễn dưới dạng Đại số quan hệ & Phép toán logic bộ)** và **Bảng tầm ảnh hưởng**.

---

## Quy Ước Ký Hiệu Trong Bảng Tầm Ảnh Hưởng
* **`+`**: Bắt buộc phải kiểm tra tính đúng đắn của ràng buộc (có thể xảy ra vi phạm).
* **`-`**: Không cần kiểm tra (không thể xảy ra vi phạm).
* **`*`**: Chỉ cần kiểm tra khi có sự thay đổi (cập nhật) ở các thuộc tính liên quan trực tiếp đến ràng buộc. Nếu cập nhật các thuộc tính khác, ký hiệu tương đương `-`.

---

# PHẦN I: RÀNG BUỘC TOÀN VẸN TRÊN MỘT QUAN HỆ

## 1. Ràng buộc miền giá trị (Domain Constraints)
Ràng buộc miền giá trị giới hạn tập hợp các giá trị hợp lệ mà một thuộc tính cụ thể có thể nhận.

### RBTV 1.1: Tỷ lệ chi trả bảo hiểm y tế (`PATIENT_INSURANCE.coverage_percent`)
* **Bối cảnh:** Quan hệ `PATIENT_INSURANCE`. Thuộc tính `coverage_percent` đại diện cho phần trăm hóa đơn được bảo hiểm chi trả.
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{\neg(coverage\_percent \ge 0 \ \wedge \ coverage\_percent \le 100)}(\text{PATIENT\_INSURANCE}) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t \in \text{PATIENT\_INSURANCE} \ (0 \le t.coverage\_percent \le 100)$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `PATIENT_INSURANCE` | $+$ | $-$ | $+ \ (coverage\_percent)$ |

---

### RBTV 1.2: Đơn giá giường bệnh (`BED.price`)
* **Bối cảnh:** Quan hệ `BED`. Thuộc tính `price` lưu đơn giá lưu trú một ngày đêm của giường bệnh.
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{price < 0}(\text{BED}) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t \in \text{BED} \ (t.price \ge 0)$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `BED` | $+$ | $-$ | $+ \ (price)$ |

---

### RBTV 1.3: Giới tính của bác sĩ (`DOCTOR.gender`)
* **Bối cảnh:** Quan hệ `DOCTOR`. Thuộc tính `gender` đại diện cho giới tính của bác sĩ.
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{gender \notin \{\text{'MALE'}, \ \text{'FEMALE'}, \ \text{'OTHER'}\}}(\text{DOCTOR}) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t \in \text{DOCTOR} \ (t.gender \in \{\text{'MALE'}, \ \text{'FEMALE'}, \ \text{'OTHER'}\})$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `DOCTOR` | $+$ | $-$ | $+ \ (gender)$ |

---

## 2. Ràng buộc liên thuộc tính (Inter-attribute Constraints)
Ràng buộc biểu diễn mối liên hệ logic giữa hai hoặc nhiều thuộc tính khác nhau trong cùng một bộ của một quan hệ.

### RBTV 2.1: Ngày hết hạn và ngày sản xuất của lô thuốc (`MEDICINE_LOT`)
* **Bối cảnh:** Quan hệ `MEDICINE_LOT`. Thuộc tính `manufacturing_date` (ngày sản xuất) và `expiry_date` (ngày hết hạn) của một lô thuốc.
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{manufacturing\_date \ge expiry\_date}(\text{MEDICINE\_LOT}) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t \in \text{MEDICINE\_LOT} \ (t.manufacturing\_date < t.expiry\_date)$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `MEDICINE_LOT` | $+$ | $-$ | $+ \ (manufacturing\_date, \ expiry\_date)$ |

---

### RBTV 2.2: Ngày xuất viện và nhập viện của bệnh nhân (`ADMISSION_REQUEST`)
* **Bối cảnh:** Quan hệ `ADMISSION_REQUEST` (Yêu cầu nhập viện). Thuộc tính `admission_date` (ngày nhập viện) và `discharge_date` (ngày xuất viện).
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{discharge\_date < admission\_date}(\text{ADMISSION\_REQUEST}) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t \in \text{ADMISSION\_REQUEST} \ (t.discharge\_date \ge t.admission\_date)$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `ADMISSION_REQUEST` | $+$ | $-$ | $+ \ (admission\_date, \ discharge\_date)$ |

---

### RBTV 2.3: Ràng buộc sở hữu hóa đơn (`PAYMENT_RECORD`)
* **Bối cảnh:** Quan hệ `PAYMENT_RECORD`. Một hóa đơn tài chính phát sinh chỉ được phép thuộc về duy nhất hoặc là một Lịch hẹn khám (`appointment_id`) hoặc là một Hồ sơ bệnh án nội/ngoại trú (`med_record_id`), không được đồng thời chứa cả hai hoặc bị bỏ trống cả hai.
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{\neg((med\_record\_id \ne \text{NULL} \ \wedge \ appointment\_id = \text{NULL}) \ \vee \ (med\_record\_id = \text{NULL} \ \wedge \ appointment\_id \ne \text{NULL}))}(\text{PAYMENT\_RECORD}) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t \in \text{PAYMENT\_RECORD} \ ((t.med\_record\_id \ne \text{NULL} \wedge t.appointment\_id = \text{NULL}) \vee (t.med\_record\_id = \text{NULL} \wedge t.appointment\_id \ne \text{NULL}))$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `PAYMENT_RECORD` | $+$ | $-$ | $+ \ (med\_record\_id, \ appointment\_id)$ |

---

## 3. Ràng buộc liên bộ (Inter-tuple Constraints)
Ràng buộc biểu diễn mối liên hệ logic giữa các bộ khác nhau trong cùng một quan hệ (ví dụ: ràng buộc khóa chính, duy nhất hoặc tính chất ràng buộc không chồng chéo thời gian).

### RBTV 3.1: Duy nhất tên vai trò người dùng (`ROLE.role_name`)
* **Bối cảnh:** Quan hệ `ROLE`. Thuộc tính `role_name` định nghĩa các chức danh trong hệ thống (như ADMIN, DOCTOR, PATIENT, ACCOUNTANT).
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{t_1.role\_id \ne t_2.role\_id \ \wedge \ t_1.role\_name = t_2.role\_name}(\text{ROLE} \times \text{ROLE}) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t_1, t_2 \in \text{ROLE} \ (t_1 \ne t_2 \implies t_1.role\_name \ne t_2.role\_name)$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `ROLE` | $+$ | $-$ | $+ \ (role\_name)$ |

---

### RBTV 3.2: Sự duy nhất lịch làm việc của một bác sĩ (`DOCTOR_SCHEDULE`)
* **Bối cảnh:** Quan hệ `DOCTOR_SCHEDULE`. Lịch trực của bác sĩ được xác định bởi tổ hợp `(doctor_id, schedule_date, shift)`. Một bác sĩ không thể nhận hai lịch khám trùng ngày và trùng ca làm việc (Sáng/Chiều).
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{t_1.doctor\_schedule\_id \ne t_2.doctor\_schedule\_id \ \wedge \ t_1.doctor\_id = t_2.doctor\_id \ \wedge \ t_1.schedule\_date = t_2.schedule\_date \ \wedge \ t_1.shift = t_2.shift}(\text{DOCTOR\_SCHEDULE} \times \text{DOCTOR\_SCHEDULE}) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t_1, t_2 \in \text{DOCTOR\_SCHEDULE} \ (t_1 \ne t_2 \implies \neg(t_1.doctor\_id = t_2.doctor\_id \wedge t_1.schedule\_date = t_2.schedule\_date \wedge t_1.shift = t_2.shift))$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `DOCTOR_SCHEDULE` | $+$ | $-$ | $+ \ (doctor\_id, \ schedule\_date, \ shift)$ |

---

### RBTV 3.3: Tính duy nhất của số thứ tự hàng đợi theo ca trực (`APPOINTMENT`)
* **Bối cảnh:** Quan hệ `APPOINTMENT` (Lịch hẹn khám). Mỗi lịch hẹn đăng ký thành công cho một ca trực của bác sĩ (`doctor_schedule_id`) sẽ được cấp một số thứ tự khám `queue_num` duy nhất. Không được có hai lịch hẹn trùng số thứ tự trong cùng một ca trực.
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{t_1.appointment\_id \ne t_2.appointment\_id \ \wedge \ t_1.doctor\_schedule\_id = t_2.doctor\_schedule\_id \ \wedge \ t_1.queue\_num = t_2.queue\_num}(\text{APPOINTMENT} \times \text{APPOINTMENT}) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t_1, t_2 \in \text{APPOINTMENT} \ (t_1 \ne t_2 \implies \neg(t_1.doctor\_schedule\_id = t_2.doctor\_schedule\_id \wedge t_1.queue\_num = t_2.queue\_num))$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `APPOINTMENT` | $+$ | $-$ | $+ \ (doctor\_schedule\_id, \ queue\_num)$ |

---

# PHẦN II: RÀNG BUỘC TOÀN VẸN TRÊN NHIỀU QUAN HỆ

## 1. Ràng buộc khóa ngoại (Foreign Key Constraints / Referential Integrity)
Đảm bảo mối quan hệ tham chiếu dữ liệu giữa khóa ngoại của quan hệ con và khóa chính của quan hệ cha.

### RBTV 4.1: Phòng bệnh phải thuộc về một Chi nhánh tồn tại (`ROOM` và `BRANCH`)
* **Bối cảnh:** Quan hệ con `ROOM` tham chiếu thuộc tính `branch_id` đến khóa chính của quan hệ cha `BRANCH`.
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\pi_{branch\_id}(\text{ROOM}) \subseteq \pi_{branch\_id}(\text{BRANCH})$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t \in \text{ROOM} \ \exists s \in \text{BRANCH} \ (t.branch\_id = s.branch\_id)$$
* **Bảng tầm ảnh hưởng:**
  *(Giả thiết áp dụng hành vi chặn xóa - RESTRICT / NO ACTION)*
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `ROOM` | $+$ | $-$ | $+ \ (branch\_id)$ |
  | `BRANCH` | $-$ | $+$ | $+ \ (branch\_id)$ |

---

### RBTV 4.2: Đơn thuốc phải tham chiếu tới Hồ sơ bệnh án (`PRESCRIPTION` và `MEDICAL_RECORD`)
* **Bối cảnh:** Mỗi đơn thuốc được cấp trong quan hệ `PRESCRIPTION` phải liên kết tương ứng với một Hồ sơ bệnh án hợp lệ trong quan hệ `MEDICAL_RECORD` qua thuộc tính `med_record_id`.
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\pi_{med\_record\_id}(\text{PRESCRIPTION}) \subseteq \pi_{med\_record\_id}(\text{MEDICAL\_RECORD})$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t \in \text{PRESCRIPTION} \ \exists s \in \text{MEDICAL\_RECORD} \ (t.med\_record\_id = s.med\_record\_id)$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `PRESCRIPTION` | $+$ | $-$ | $+ \ (med\_record\_id)$ |
  | `MEDICAL_RECORD` | $-$ | $+$ | $+ \ (med\_record\_id)$ |

---

### RBTV 4.3: Hồ sơ bác sĩ phải gắn liền với Tài khoản hệ thống (`DOCTOR` và `ACCOUNT`)
* **Bối cảnh:** Quan hệ con `DOCTOR` liên kết với tài khoản người dùng tại quan hệ cha `ACCOUNT` qua thuộc tính `account_id` để kiểm soát đăng nhập và phân quyền.
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\pi_{account\_id}(\text{DOCTOR}) \subseteq \pi_{account\_id}(\text{ACCOUNT})$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t \in \text{DOCTOR} \ \exists s \in \text{ACCOUNT} \ (t.account\_id = s.account\_id)$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `DOCTOR` | $+$ | $-$ | $+ \ (account\_id)$ |
  | `ACCOUNT` | $-$ | $+$ | $+ \ (account\_id)$ |

---

## 2. Ràng buộc liên thuộc tính - liên quan hệ (Inter-attribute Inter-relation Constraints)
Ràng buộc thể hiện mối liên hệ phụ thuộc logic giữa các thuộc tính nằm ở các quan hệ khác nhau.

### RBTV 5.1: Đồng bộ đơn giá khám trong Lịch hẹn khám (`APPOINTMENT` và `CONSULTATION_FEE`)
* **Bối cảnh:** Khi bệnh nhân đặt lịch hẹn khám (`APPOINTMENT`), đơn giá snapshot lưu tại thời điểm đặt (`fee_price_snapshot`) phải bằng chính xác giá khám hiện hành (`price`) của mã phí khám (`fee_id`) trong quan hệ `CONSULTATION_FEE`.
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\pi_{appointment\_id, \ fee\_price\_snapshot, \ price}(\text{APPOINTMENT} \bowtie_{\text{fee\_id}} \text{CONSULTATION\_FEE}) \implies \sigma_{fee\_price\_snapshot \ne price}(\text{APPOINTMENT} \bowtie_{\text{fee\_id}} \text{CONSULTATION\_FEE}) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t \in \text{APPOINTMENT}, \ \forall s \in \text{CONSULTATION\_FEE} \ (t.fee\_id = s.fee\_id \implies t.fee\_price\_snapshot = s.price)$$
* **Bảng tầm ảnh hưởng:**
  > [!NOTE]
  > Đây là dạng ràng buộc nghiệp vụ mang tính chất lưu vết lịch sử (snapshot). Sự thay đổi giá của dịch vụ trong tương lai ở bảng `CONSULTATION_FEE` không được làm hồi tố thay đổi các hóa đơn/lịch hẹn trong quá khứ ở bảng `APPOINTMENT`. Do đó, tầm ảnh hưởng chỉ kiểm tra khi thêm mới lịch hẹn hoặc chỉnh sửa thuộc tính liên quan trực tiếp ở bảng con.
  
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `APPOINTMENT` | $+$ | $-$ | $+ \ (fee\_id, \ fee\_price\_snapshot)$ |
  | `CONSULTATION_FEE` | $-$ | $-$ | $-$ |

---

### RBTV 5.2: Tính nhất quán tổng chi phí điều trị của hồ sơ bệnh án
* **Bối cảnh:** Tổng giá tiền điều trị của hồ sơ bệnh án (`MEDICAL_RECORD.total_price`) phải bằng tổng các đơn giá snapshot (`snapshot_price`) của tất cả các dịch vụ và xét nghiệm được chỉ định đi kèm thuộc về hồ sơ bệnh án đó.
* **Nội dung:**
  * **Phép toán logic (Tuple Calculus):**
    $$\forall m \in \text{MEDICAL\_RECORD} \ \left( m.total\_price = \sum_{t \in \text{LabItems}} t.snapshot\_price + \sum_{s \in \text{SerItems}} s.snapshot\_price \right)$$
    Trong đó:
    * $\text{LabItems} = \{ l_i \ | \ \exists l \in \text{LAB\_TEST\_REQUEST} \ (l.med\_record\_id = m.med\_record\_id \ \wedge \ l_i.lab\_test\_request\_id = l.lab\_test\_request\_id) \}$
    * $\text{SerItems} = \{ s_i \ | \ \exists s \in \text{MEDICAL\_SERVICE\_REQUEST} \ (s.med\_record\_id = m.med\_record\_id \ \wedge \ s_i.med\_ser\_req\_id = s.med\_ser\_req\_id) \}$
* **Bảng tầm ảnh hưởng:**
  *(Mỗi khi có hoạt động thêm/xóa/sửa các xét nghiệm hay dịch vụ cận lâm sàng của bệnh án, hệ thống phải kích hoạt hàm cập nhật đồng bộ tổng giá trị hóa đơn).*
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `MEDICAL_RECORD` | $+$ | $-$ | $+ \ (total\_price)$ |
  | `LAB_TEST_REQUEST_ITEM` | $+$ | $+$ | $+ \ (snapshot\_price)$ |
  | `MEDICAL_SERVICE_REQUEST_ITEM` | $+$ | $+$ | $+ \ (snapshot\_price)$ |

---

### RBTV 5.3: Trạng thái thanh toán khi hoàn thành lịch hẹn khám (`APPOINTMENT` và `PAYMENT_RECORD`)
* **Bối cảnh:** Khi một lịch hẹn khám có trạng thái chuyển sang hoàn thành (`APPOINTMENT.status = 'COMPLETED'`), thì bản ghi hóa đơn liên kết tương ứng (`PAYMENT_RECORD`) buộc phải có trạng thái thanh toán là đã thanh toán (`payment_status = 'PAID'`).
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{apt.status = \text{'COMPLETED'} \ \wedge \ pay.payment\_status \ne \text{'PAID'}}(\text{APPOINTMENT} \ apt \bowtie_{\text{appointment\_id}} \text{PAYMENT\_RECORD} \ pay) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall t \in \text{APPOINTMENT}, \ \forall p \in \text{PAYMENT\_RECORD} \ (t.appointment\_id = p.appointment\_id \ \wedge \ t.status = \text{'COMPLETED'} \implies p.payment\_status = \text{'PAID'})$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `APPOINTMENT` | $-$ | $-$ | $+ \ (status)$ |
  | `PAYMENT_RECORD` | $-$ | $-$ | $+ \ (payment\_status)$ |

---

## 3. Ràng buộc liên bộ - liên quan hệ (Inter-tuple Inter-relation Constraints)
Ràng buộc thể hiện các logic nghiệp vụ phức tạp liên quan đến nhiều bộ dữ liệu ở các bảng khác nhau.

### RBTV 6.1: Nhất quán về bác sĩ điều trị và bác sĩ trực ca
* **Bối cảnh:** Bác sĩ chịu trách nhiệm thực hiện khám và lập hồ sơ bệnh án (`MEDICAL_RECORD.doctor_id`) phải là bác sĩ được phân công phụ trách ca trực của cuộc hẹn đó (`DOCTOR_SCHEDULE.doctor_id` thông qua khóa nối `APPOINTMENT`).
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{mr.doctor\_id \ne ds.doctor\_id}\left( \text{MEDICAL\_RECORD} \ mr \bowtie_{apt} \text{APPOINTMENT} \ apt \bowtie_{ds} \text{DOCTOR\_SCHEDULE} \ ds \right) = \emptyset$$
    *(Phép nối tự nhiên thực hiện trên các trường tương ứng: `mr.appointment_id = apt.appointment_id` và `apt.doctor_schedule_id = ds.doctor_schedule_id`)*
  * **Phép toán logic (Tuple Calculus):**
    $$\forall mr \in \text{MEDICAL\_RECORD}, \ \forall apt \in \text{APPOINTMENT}, \ \forall ds \in \text{DOCTOR\_SCHEDULE} \ \left( \begin{aligned} &(mr.appointment\_id = apt.appointment\_id \\ &\wedge \ apt.doctor\_schedule\_id = ds.doctor\_schedule\_id) \\ &\implies mr.doctor\_id = ds.doctor\_id \end{aligned} \right)$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `MEDICAL_RECORD` | $+$ | $-$ | $+ \ (doctor\_id, \ appointment\_id)$ |
  | `APPOINTMENT` | $-$ | $-$ | $+ \ (doctor\_schedule\_id)$ |
  | `DOCTOR_SCHEDULE` | $-$ | $-$ | $+ \ (doctor\_id)$ |

---

### RBTV 6.2: Đồng bộ chuyên khoa của phòng khám trực và bác sĩ
* **Bối cảnh:** Khi xếp lịch trực cho bác sĩ (`DOCTOR_SCHEDULE`), chuyên khoa phòng khám được chỉ định (`ROOM.specialty_id` của phòng trực) phải trùng khớp hoàn toàn với chuyên khoa công tác của bác sĩ đảm nhận ca trực đó (`DOCTOR.specialty_id`).
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{room.specialty\_id \ne doc.specialty\_id}\left( \text{DOCTOR\_SCHEDULE} \ ds \bowtie_{room\_id} \text{ROOM} \ room \bowtie_{doctor\_id} \text{DOCTOR} \ doc \right) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall ds \in \text{DOCTOR\_SCHEDULE}, \ \forall r \in \text{ROOM}, \ \forall d \in \text{DOCTOR} \ \left( \begin{aligned} &(ds.room\_id = r.room\_id \ \wedge \ ds.doctor\_id = d.doctor\_id) \\ &\implies r.specialty\_id = d.specialty\_id \end{aligned} \right)$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `DOCTOR_SCHEDULE` | $+$ | $-$ | $+ \ (room\_id, \ doctor\_id)$ |
  | `ROOM` | $-$ | $-$ | $+ \ (specialty\_id)$ |
  | `DOCTOR` | $-$ | $-$ | $+ \ (specialty\_id)$ |

---

### RBTV 6.3: Đồng bộ trạng thái giường bệnh khi nhập viện điều trị nội trú
* **Bối cảnh:** Khi bệnh nhân có phiếu yêu cầu nhập viện ở trạng thái đã nhập viện điều trị (`ADMISSION_REQUEST.status = 'ADMITTED'`), thì trạng thái vật lý của chiếc giường được gán tương ứng (`BED.status`) phải tự động chuyển sang trạng thái bận (`status = 'OCCUPIED'`).
* **Nội dung:**
  * **Đại số quan hệ (RA):**
    $$\sigma_{ar.status = \text{'ADMITTED'} \ \wedge \ b.status \ne \text{'OCCUPIED'}}\left( \text{ADMISSION\_REQUEST} \ ar \bowtie_{bed\_id} \text{BED} \ b \right) = \emptyset$$
  * **Phép toán logic (Tuple Calculus):**
    $$\forall ar \in \text{ADMISSION\_REQUEST}, \ \forall b \in \text{BED} \ \left( ar.bed\_id = b.bed\_id \ \wedge \ ar.status = \text{'ADMITTED'} \implies b.status = \text{'OCCUPIED'} \right)$$
* **Bảng tầm ảnh hưởng:**
  | Quan hệ | Thêm (Insert) | Xóa (Delete) | Sửa (Update) |
  | :--- | :---: | :---: | :---: |
  | `ADMISSION_REQUEST` | $+$ | $-$ | $+ \ (bed\_id, \ status)$ |
  | `BED` | $-$ | $-$ | $+ \ (status)$ |
