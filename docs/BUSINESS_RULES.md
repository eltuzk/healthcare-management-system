# Business Rules - Healthcare Management System

Tai lieu nay tong hop cac business rule va validation rule dang duoc the hien trong code hien tai. Khi sua service, DTO validation hoac migration Flyway, can cap nhat file nay de giu rule dong bo voi source code.

## 1. Quy Uoc Chung

- `is_active = 1` hoac `active = true`: ban ghi dang hoat dong.
- `is_active = 0` hoac `active = false`: ban ghi bi vo hieu hoa/soft delete.
- Cac API lay danh sach chinh thuong chi tra ve ban ghi dang active doi voi account, doctor, patient.
- Cac ma/ten master data quan trong phai duy nhat de tranh nhap trung.
- Cac bang co `version_number` dung optimistic locking de tranh ghi de khi nhieu request cap nhat cung luc.
- Cac field `*_snapshot` luu gia tri tai thoi diem phat sinh nghiep vu, khong tinh lai theo master data sau nay.

## 2. Xac Thuc, Tai Khoan Va Phan Quyen

### Dang ky va kich hoat tai khoan

- Email dang ky phai dung dinh dang email va khong duoc trung.
- Mat khau phai co toi thieu 8 ky tu, it nhat 1 chu hoa va 1 chu so.
- Tai khoan dang ky moi duoc gan role `PATIENT`.
- Tai khoan dang ky moi co `is_active = 0` cho den khi xac thuc email.
- Token xac thuc email phai hop le va con han.
- Tai khoan da active khong duoc kich hoat lai.

### Dang nhap va mat khau

- Chi tai khoan active moi duoc dang nhap.
- Sai email hoac mat khau deu tra ve loi chung, khong tiet lo email co ton tai hay khong.
- Quen mat khau chi gui email reset neu email ton tai.
- Reset password yeu cau token hop le va con han.
- Mat khau moi va xac nhan mat khau moi phai trung nhau.
- Mat khau moi khong duoc trung voi mat khau cu.
- Doi mat khau yeu cau tai khoan active, dung mat khau cu, va mat khau moi khong trung mat khau cu.

### Quan ly account

- Email account la duy nhat.
- Tao account tu admin/back-office tao tai khoan active ngay (`is_active = 1`).
- Account phai gan voi mot role ton tai.
- Xoa account la soft delete bang cach set `is_active = 0`.
- Khi soft delete account, doctor/patient lien ket voi account do cung bi vo hieu hoa.

### Role va permission

- `role_name` la duy nhat.
- `permission_name` la duy nhat.
- Khong duoc xoa role neu dang duoc gan cho account.
- Khong duoc xoa permission neu dang duoc gan cho role hoac account.
- Mot role khong duoc gan trung cung mot permission.
- Mot account khong duoc gan trung cung mot permission.
- Khong gan permission truc tiep cho account neu role cua account da co permission do.
- Quyen thuc te cua user = quyen theo role + quyen bo sung theo account.

## 3. Co So Vat Chat

### Chi nhanh, loai phong, phong

- Ten chi nhanh (`branch_name`) phai duy nhat.
- Ten loai phong (`room_type_name`) phai duy nhat.
- Ma phong (`room_code`) phai duy nhat.
- Phong phai thuoc mot chi nhanh va mot loai phong ton tai.
- Khong duoc xoa loai phong neu dang co phong su dung.
- Khong duoc xoa phong neu trong phong dang co giuong.

### Giuong

- Giuong phai thuoc mot phong ton tai.
- Khi tao giuong, trang thai mac dinh la `AVAILABLE`.
- Gia giuong khong duoc de trong va ve database phai >= 0.
- Trang thai giuong hop le: `AVAILABLE`, `OCCUPIED`, `MAINTENANCE`.

## 4. Chuyen Khoa, Bac Si, Benh Nhan

### Chuyen khoa

- `specialty_code` khong duoc trong, toi da 50 ky tu, va duy nhat khong phan biet hoa thuong.
- `specialty_name` khong duoc trong, toi da 200 ky tu, va duy nhat khong phan biet hoa thuong.
- Deactivate chuyen khoa set `is_active = 0`.
- Cac nghiep vu tao/cap nhat bac si hoac phi kham chi duoc chon chuyen khoa dang active.

### Bac si

- Bac si phai lien ket voi mot account active.
- Moi account chi duoc lien ket voi toi da mot bac si.
- So giay phep hanh nghe (`license_num`) bat buoc va duy nhat.
- So CCCD/CMND (`identity_num`) neu co thi phai duy nhat.
- Bac si bat buoc co chuyen khoa active.
- Khi gan chuyen khoa, `specialization` duoc dong bo theo ten chuyen khoa va chi mang tinh hien thi/legacy.
- Tinh phi kham phai dua tren `doctor.specialty_id`, khong dua tren text `specialization`.
- Gender hop le: `MALE`, `FEMALE`, `OTHER`.
- Ngay sinh va ngay vao lam khong duoc lon hon ngay hien tai.
- Xoa bac si la soft delete (`active = false`).

### Benh nhan

- Ho ten benh nhan khong duoc de trong.
- Benh nhan co the co hoac khong co account, de ho tro walk-in.
- Neu co account, account phai active va moi account chi duoc lien ket voi toi da mot benh nhan.
- So CCCD/CMND neu co thi phai duy nhat.
- Gender hop le: `MALE`, `FEMALE`, `OTHER`.
- Xoa benh nhan la soft delete (`is_active = 0`).

### Bao hiem benh nhan

- Bao hiem phai thuoc mot benh nhan active.
- So bao hiem khong duoc trong va phai duy nhat.
- Ty le thanh toan (`coverage_percent`) phai nam trong khoang 0 den 100.
- Ngay het han khong duoc de trong.
- Trang thai mac dinh khi tao la `ACTIVE` neu request khong truyen status.
- Moi benh nhan chi duoc co toi da mot bao hiem `ACTIVE`.
- Trang thai bao hiem hop le theo database: `ACTIVE`, `EXPIRED`, `SUSPENDED`.

## 5. Phi Kham

- `fee_code` khong duoc trong, toi da 50 ky tu, va duy nhat khong phan biet hoa thuong.
- `fee_name` khong duoc trong va toi da 200 ky tu.
- Phi kham bat buoc lien ket voi mot chuyen khoa active.
- Moi chuyen khoa chi duoc co mot ban ghi phi kham hien hanh trong `CONSULTATION_FEE`.
- `price` khong duoc null va ve database phai >= 0.
- Deactivate phi kham set `is_active = 0`.
- `specialty_id` la FK chuan de tinh tien.
- Field text `specialty` chi la legacy/hien thi va duoc dong bo tu ten chuyen khoa.
- Khi tao appointment, ten phi va gia phi duoc snapshot vao appointment.

## 6. Lich Lam Viec Bac Si

- Lich lam viec phai co bac si, phong, ngay lam viec, ca lam viec va suc chua toi da.
- Ngay lam viec khong duoc nam trong qua khu.
- Suc chua toi da (`max_capacity`) phai > 0 va khong vuot qua 99999.
- Khi cap nhat lich, `max_capacity` khong duoc nho hon `current_booking_count`.
- Ca lam viec hop le: `MORNING`, `AFTERNOON`.
- Mot bac si khong duoc co hai lich trung cung ngay va cung ca.
- Mot phong khong duoc duoc phan cho hai bac si trong cung ngay va cung ca.
- Khi tao appointment, service lock row lich kham de cap nhat `current_booking_count` va `last_queue_number`.
- `last_queue_number` chi tang, khong rollback khi huy/het han thanh toan; queue co the co khoang trong.
- Hien tai khong ho tro xoa lich lam viec bac si.
- Chuc nang import lich lam viec bac si chua hoan tat.

## 7. Appointment

### Tao appointment online

- Request bat buoc co `patientId`, `doctorScheduleId`, `initialSymptoms`, `visitReason`.
- `initialSymptoms` toi da 4000 ky tu.
- `visitReason` toi da 500 ky tu.
- Benh nhan phai ton tai va dang active.
- Lich kham phai ton tai, chua het han, va chua day.
- Benh nhan khong duoc co appointment active khac.
- Appointment active gom: `PENDING`, `CONFIRMED`, `CHECKED_IN`, `IN_PROGRESS`.
- Bac si cua lich kham phai co chuyen khoa de resolve phi kham.
- Phai ton tai phi kham active cho chuyen khoa cua bac si.
- Tao online appointment se:
  - Giu slot ngay lap tuc.
  - Tang `current_booking_count`.
  - Tang va cap `queue_num`.
  - Tao `appointment_code` dang `APT-XXXXXXXX`.
  - Snapshot ten phi va gia phi.
  - Set status `PENDING`.
  - Set `payment_expires_at = now + 10 minutes`.
  - Tao `PAYMENT_RECORD` owner appointment voi status `UNPAID`.

### Tao appointment walk-in da thu tien

- Walk-in yeu cau receptionist/account dang dang nhap.
- Request bat buoc co `patientId`, `doctorScheduleId`, `receivedAmount`, `initialSymptoms`, `visitReason`.
- So tien thu (`receivedAmount`) phai khop chinh xac voi gia phi kham.
- Walk-in tao appointment o status `CHECKED_IN`, khong di qua `CONFIRMED`.
- Walk-in set `paid_at` va `checked_in_at` ngay khi tao.
- Walk-in tao `PAYMENT_RECORD` status `PAID` va `PAYMENT_TRANSACTION` gateway `CASH`.
- Cash transaction luu nguoi thu/xac nhan la account hien tai.

### Thanh toan SePay

- Webhook SePay phai co secret hop le.
- Neu cau hinh `sepay.account-number`, account number webhook phai khop.
- Chi transfer type `in` moi duoc confirm appointment.
- Transfer amount phai > 0.
- Appointment code duoc lay tu field `code` hoac trich xuat tu `content` theo pattern `APT-[A-Z0-9]{8}`.
- Chi appointment status `PENDING` moi duoc confirm thanh toan.
- Appointment da `CONFIRMED` thi khong confirm lai nhu request moi.
- Thanh toan phai truoc `payment_expires_at`.
- Transfer amount phai khop chinh xac voi tong tien appointment can thu.
- Confirm thanh toan thanh cong se:
  - Set appointment status `CONFIRMED`.
  - Set `paid_at`.
  - Luu SePay transaction id/reference/content.
  - Cap nhat payment record thanh `PAID`.
  - Tao `PAYMENT_TRANSACTION` gateway `SEPAY`.
- Neu trung SePay transaction id, service tra ve appointment da duoc ghi nhan truoc do de dam bao idempotency.

### Check-in, bat dau kham, huy va het han

- Chi appointment `CONFIRMED` moi duoc check-in.
- Check-in set status `CHECKED_IN` va `checked_in_at`.
- Chi appointment `CHECKED_IN` moi duoc start.
- Start set status `IN_PROGRESS`.
- Appointment da thanh toan hoac dang kham/da xong khong duoc huy.
- Chi appointment `PENDING` moi duoc huy thu cong.
- Huy appointment release slot bang cach giam `current_booking_count`, nhung khong giam `last_queue_number`.
- Scheduler chuyen appointment `PENDING` qua han thanh toan sang `PAYMENT_EXPIRED` va release slot.

### Trang thai appointment

- `PENDING`: online booking da giu slot, cho thanh toan.
- `CONFIRMED`: online booking da thanh toan.
- `CHECKED_IN`: benh nhan da co mat hoac walk-in da thu tien.
- `IN_PROGRESS`: bac si dang kham.
- `COMPLETED`: hoan tat kham.
- `CANCELLED`: huy lich chua thanh toan.
- `PAYMENT_EXPIRED`: online booking qua han thanh toan.

## 8. Ho So Benh An

- Chi tao medical record khi appointment dang `IN_PROGRESS`.
- Moi appointment chi co toi da mot medical record.
- Bac si thuong chi duoc tao/sua medical record cua appointment thuoc lich bac si do.
- Admin duoc truy cap theo appointment owner doctor.
- Tao medical record ban dau set status `DRAFT`.
- Tao medical record bat buoc co `initialDiagnosis`, toi da 1000 ky tu.
- Draft medical record chua bat buoc co `clinicalConclusion` va `conclusionType`.
- Khi update medical record, client bat buoc gui `version` dang thay.
- Neu version request khac `version_number` hien tai, reject de tranh ghi de.
- Medical record chi sua duoc khi status la `DRAFT` hoac `IN_PROGRESS`.
- Update noi dung medical record khong tu chuyen `DRAFT` sang `IN_PROGRESS`.
- Bac si chi duoc tao phieu xet nghiem/dich vu khi medical record dang `DRAFT`.
- Sau khi tao xong cac phieu chi dinh, benh nhan phai thanh toan tong tien medical record.
- Ghi nhan thanh toan medical record thanh cong se set payment record `PAID` va chuyen medical record tu `DRAFT` sang `IN_PROGRESS`.
- Chi duoc cap nhat trang thai/ket qua cac phieu xet nghiem/dich vu khi medical record dang `IN_PROGRESS` va da thanh toan du.
- Complete medical record bat buoc co:
  - `initialDiagnosis`
  - `clinicalConclusion`
  - `conclusionType`
- Complete medical record chi duoc thuc hien khi tat ca phieu xet nghiem/dich vu da co ket qua.
- Complete medical record set status `COMPLETED`, set `completed_at`, va dong thoi set appointment thanh `COMPLETED`.
- Chi medical record `COMPLETED` moi duoc lock.
- Lock medical record set status `LOCKED` va `locked_at`.
- Medical record `COMPLETED` hoac `LOCKED` khong duoc sua.
- Medical record chi eligible tao phieu nhap vien khi `conclusionType = ADMISSION_REQUIRED`.
- `MedicalRecordConclusionType.COMPLETED` la kieu ket luan dieu tri ngoai tru/ket thuc kham, khong phai status cua ho so.

### Trang thai medical record

- `DRAFT`: ho so moi tao, chua day du thong tin kham.
- `IN_PROGRESS`: ho so dang duoc cap nhat.
- `COMPLETED`: ho so da hoan tat chuyen mon.
- `LOCKED`: ho so da khoa, khong cho sua.

### Phieu xet nghiem va dich vu

- Phieu xet nghiem/dich vu chi duoc tao khi medical record dang `DRAFT`.
- Khi tao phieu, service lock medical record truoc khi validate va sync billing.
- Moi phieu phai co it nhat mot item duoc chi dinh.
- Gia cua tung item phai duoc snapshot tai thoi diem chi dinh, khong tinh lai theo master data sau nay.
- Tong tien medical record va payment record medical record duoc sync lai sau moi lan tao phieu.
- Sau khi da ghi nhan thanh toan medical record va medical record chuyen sang `IN_PROGRESS`, khong duoc tao them phieu moi.
- Chi duoc cap nhat status hoac ket qua phieu khi medical record dang `IN_PROGRESS` va payment record medical record da `PAID`.
- Cap nhat status phieu bang tay chi duoc chuyen sang `SAMPLE_COLLECTED`.
- Khong duoc cap nhat status phieu da `RESULT_AVAILABLE`.
- Khong duoc tao result neu phieu chua o status `SAMPLE_COLLECTED`.
- Tao result thanh cong se tu dong set phieu sang `RESULT_AVAILABLE`.
- Moi phieu chi co toi da mot result; tao result trung se bi reject.
- Sua result chi duoc phep khi medical record van dang `IN_PROGRESS` va da thanh toan du.
- Sau moi lan tao result, he thong tu kiem tra neu tat ca phieu xet nghiem/dich vu deu da `RESULT_AVAILABLE` va medical record da du thong tin bat buoc thi tu complete medical record.

### Trang thai phieu xet nghiem/dich vu

- `NOT_COLLECTED`: da chi dinh, chua lay mau/chua thuc hien.
- `SAMPLE_COLLECTED`: da lay mau/da thuc hien, dang cho ket qua.
- `RESULT_AVAILABLE`: da co ban ghi ket qua.

## 9. Thanh Toan Va Ke Toan

- `PAYMENT_RECORD` la so cai thanh toan, duoc tao/cap nhat qua business flow.
- Payment record phai thuoc dung mot owner: hoac `appointment_id`, hoac `med_record_id`.
- Payment record appointment duoc tao khi tao appointment online/walk-in.
- Payment record medical record duoc tao khi tao medical record va gom cac chi phi xet nghiem/dich vu phat sinh.
- Thanh toan medical record yeu cau so tien khop chinh xac voi tong tien cac phieu chi dinh.
- API ghi nhan thanh toan medical record chi cho thu tien mat sau khi medical record dang `DRAFT` va da co tong tien can thu.
- Ghi nhan thanh toan medical record phai lock medical record, sync lai billing, lock payment record, validate amount, tao transaction, roi chuyen medical record sang `IN_PROGRESS`.
- `request_code` cua payment record appointment hien dung `appointment_code`.
- `total_price` va `received_amount` phai >= 0.
- Payment status hop le: `UNPAID`, `PARTIAL`, `PAID`.
- Payment transaction phai thuoc mot payment record.
- `transfer_amount` cua transaction phai > 0.
- Process status transaction hop le: `PENDING`, `SUCCESS`, `FAILED`; flow hien tai tao transaction thanh cong voi `SUCCESS`.
- `sepay_transaction_id` la duy nhat neu co.
- Gateway `SEPAY` dung cho online banking webhook.
- Gateway `CASH` dung cho walk-in thu tien mat.
- Payment appointment yeu cau so tien khop chinh xac voi phi kham.

### Bao cao doanh thu

- Bao cao doanh thu chi tinh cac `PAYMENT_TRANSACTION` co `process_status = SUCCESS`.
- Doanh thu duoc tinh theo `transaction_date`, khong theo ngay tao payment record.
- Bao cao ho tro filter theo `fromDate`, `toDate`, `gateway` va owner type (`APPOINTMENT`, `MEDICAL_RECORD`).
- Neu khong truyen khoang ngay, bao cao mac dinh lay tu dau thang hien tai den ngay hien tai.
- Bao cao tra tong doanh thu, so giao dich, breakdown theo ngay, gateway va owner type.
- API bao cao doanh thu la read-only va chi danh cho `ADMIN`, `ACCOUNTANT`.

## 10. Cac Rang Buoc Du Lieu Chinh

- `ROLE.role_name` unique.
- `PERMISSION.permission_name` unique.
- `ACCOUNT.email` unique.
- `ROLE_PERMISSION` co khoa chinh `(role_id, permission_id)`.
- `ACCOUNT_PERMISSION` co khoa chinh `(account_id, permission_id)`.
- `BRANCH.branch_name` unique.
- `ROOM_TYPE.room_type_name` unique.
- `ROOM.room_code` unique.
- `SPECIALTY.specialty_code` unique va not null.
- `SPECIALTY.specialty_name` unique va not null.
- `DOCTOR.account_id` unique.
- `DOCTOR.license_num` unique va not null.
- `DOCTOR.identity_num` unique neu co.
- `PATIENT.account_id` unique neu co.
- `PATIENT.identity_num` unique neu co.
- `PATIENT_INSURANCE.insurance_num` unique va not null.
- `CONSULTATION_FEE.fee_code` unique va not null.
- `CONSULTATION_FEE.specialty_id` unique neu co.
- `DOCTOR_SCHEDULE` unique theo `(doctor_id, schedule_date, shift)`.
- `DOCTOR_SCHEDULE` unique theo `(room_id, schedule_date, shift)`.
- `APPOINTMENT.appointment_code` unique va not null.
- `APPOINTMENT` unique theo `(doctor_schedule_id, queue_num)`.
- `MEDICAL_RECORD.appointment_id` unique.
- `PAYMENT_RECORD.appointment_id` unique neu co.
- `PAYMENT_RECORD.med_record_id` unique neu co.
- `PAYMENT_RECORD.request_code` unique va not null.
- `PAYMENT_TRANSACTION.sepay_transaction_id` unique neu co.

## 11. Nguyen Tac Bao Toan Trang Thai

- Booking appointment, giu slot va tao payment record phai nam trong cung transaction.
- Walk-in thu tien, tao appointment, tao payment record va cash transaction phai commit cung nhau.
- Webhook SePay validate, khoa appointment, cap nhat paid va ghi transaction phai commit cung nhau.
- Huy/het han appointment phai release slot trong cung transaction voi doi status.
- Complete medical record va complete appointment phai di cung nhau.
- Tao phieu xet nghiem/dich vu, sync billing va ghi nhan thanh toan medical record phai lock medical record de tranh tong tien bi lech.
- Cap nhat status/result cua phieu phai lock phieu can cap nhat va validate lai medical record/payment trong cung transaction.
- Tao result cuoi cung va auto-complete medical record phai nam trong cung transaction.
- Queue number da cap khong duoc tai su dung de giu audit trail.
- Gia phi kham cua appointment khong duoc tinh lai sau khi master data thay doi; phai dung snapshot tren appointment/payment record.
