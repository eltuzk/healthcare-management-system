<p align="center"><img width="454" height="126" alt="image" src="https://github.com/user-attachments/assets/2036c003-62d1-42f1-9817-6cca86de0fc8" /> </p>

## [GIỚI THIỆU ĐỒ ÁN](#)

* **Đề tài:** Xây dựng Hệ thống Quản lý Y tế (Healthcare Management System)
* **Mô tả tổng quan:** Đề tài "Xây dựng Hệ thống Quản lý Y tế" là một dự án phần mềm web toàn diện, được thiết kế để giải quyết bài toán vận hành của mô hình y tế đa chi nhánh hiện đại. Hệ thống hỗ trợ **7 vai trò** (Quản trị viên, Bác sĩ, Bệnh nhân, Lễ tân, Kỹ thuật viên, Dược sĩ, Kế toán) với **31 use-case** bao phủ toàn bộ quy trình: tiếp nhận bệnh nhân, quản lý lịch khám, khám chữa bệnh, xét nghiệm, dịch vụ chức năng, nội trú, kê đơn thuốc, kho thuốc, thanh toán và báo cáo doanh thu.

## [CÁC TÍNH NĂNG VÀ NGHIỆP VỤ NỔI BẬT](#)

Hệ thống được chia thành các phân hệ lõi với hàm lượng kỹ thuật cao:

* **[Quản lý nhân sự & Phân quyền (RBAC):](#)** Triển khai cơ chế phân quyền RBAC chặt chẽ với Role và Permission độc lập. Hỗ trợ gán quyền bổ sung cấp tài khoản ngoài quyền mặc định của vai trò. Quản lý toàn bộ vòng đời tài khoản nhân sự nội bộ với soft-delete.

* **[Quản lý lịch khám & Đặt lịch hẹn:](#)** Bác sĩ đăng ký lịch làm việc theo ca, có kiểm soát số lượng đặt tối đa (MaxCapacity). Bệnh nhân đặt lịch trực tuyến, hệ thống tự động cấp số thứ tự (QueueNum) và theo dõi trạng thái lịch hẹn theo thời gian thực.

* **[Khám chữa bệnh & Hồ sơ bệnh án:](#)** Bác sĩ lập bệnh án điện tử, chỉ định xét nghiệm/dịch vụ chức năng, kê đơn thuốc và chỉ định nhập viện trực tiếp trong luồng khám. Kết quả xét nghiệm và dịch vụ được ghi nhận bởi Kỹ thuật viên và liên kết với bệnh án.

* **[Quản lý nội trú:](#)** Quản lý phòng, giường bệnh theo loại phòng và chi nhánh. Tự động cập nhật trạng thái giường (AVAILABLE/OCCUPIED/MAINTENANCE) khi xử lý yêu cầu nhập/xuất viện.

* **[Quản lý kho thuốc & Đơn thuốc:](#)** Dược sĩ quản lý danh mục thuốc, theo dõi lô thuốc (MedicineLot) với ngày sản xuất và hạn sử dụng. Cấp phát đơn thuốc theo chỉ định của bác sĩ, có kiểm soát tồn kho.

* **[Thanh toán & Báo cáo doanh thu:](#)** Kế toán thu tiền và xuất hóa đơn với đa hình thức thanh toán (PaymentTransaction). Hỗ trợ bảo hiểm y tế (PatientInsurance) với tỷ lệ chi trả linh hoạt. Báo cáo doanh thu theo chi nhánh và kỳ.

* **[Bảo mật JWT & Spring Security:](#)** Xác thực stateless bằng Bearer Token. Phân quyền endpoint theo vai trò, bảo vệ toàn bộ API với cơ chế filter chain và custom exception handling.

## [CÔNG NGHỆ VÀ CÔNG CỤ SỬ DỤNG](#)

**Application Development**
* [Java](https://www.oracle.com/java/) - Ngôn ngữ lập trình chính
* [Spring Boot](https://spring.io/projects/spring-boot) - Framework phát triển backend
* [Spring Security + JWT](https://spring.io/projects/spring-security) - Xác thực và phân quyền
* [Maven](https://maven.apache.org/) - Quản lý dependency và build
* [Lombok](https://projectlombok.org/) - Giảm boilerplate code
* [Kiến trúc 5-Layer](https://en.wikipedia.org/wiki/Multitier_architecture) - Controller → Service → Repository → Entity + DTO

**Database & Tools**
* [Oracle Database](https://www.oracle.com/) - Hệ quản trị cơ sở dữ liệu
* [JPA / Hibernate](https://hibernate.org/) - ORM framework
* [VSCode](https://code.visualstudio.com/) - IDE phát triển chính
* [Flyway](https://flywaydb.org/) - Quản lý migration database
* [GitHub](https://github.com/) - Quản lý mã nguồn và làm việc nhóm

**Frontend**
* [React + Vite](https://vitejs.dev/) - Framework frontend
* [TypeScript](https://www.typescriptlang.org/) - Ngôn ngữ frontend
* [Tailwind CSS](https://tailwindcss.com/) - Styling

**Third-party Services**
* [SePay](https://sepay.vn/) - Cổng thanh toán tự động
* [DigitalOcean](https://www.digitalocean.com/) - Nền tảng triển khai (Cloud Hosting)

## [CẤU TRÚC DỰ ÁN](#)

```text
com.healthcare.backend
├── config/                  ← Cấu hình hệ thống (Security, OpenAPI, CORS,...)
├── controller/              ← Nhận HTTP request, trả về HTTP response
├── dto/
│   ├── request/             ← DTO nhận dữ liệu từ client
│   └── response/            ← DTO trả dữ liệu về cho client
├── entity/                  ← Ánh xạ bảng trong Oracle DB (JPA Entities)
├── exception/               ← Xử lý lỗi tập trung (Global Exception Handling)
├── mapper/                  ← MapStruct: Chuyển đổi qua lại giữa Entity và DTO
├── repository/              ← Tương tác với cơ sở dữ liệu (Spring Data JPA)
├── scheduler/               ← Các tác vụ chạy tự động (VD: Hủy lịch chưa thanh toán)
├── security/                ← Xử lý bảo mật JWT (Filter, Provider, Service)
└── service/
    ├── (interface)          ← Định nghĩa các nghiệp vụ (Contract)
    └── impl/                ← Triển khai logic nghiệp vụ chi tiết
```

## [THÀNH VIÊN NHÓM](#)

| STT | MSSV | Họ và Tên | GitHub | Email |
| :--- | :--- | :--- | :--- | :--- |
| 1 | 24520593 | Lại Trình Phước Hưng | https://github.com/ltrphung0620 | 24520593@gm.uit.edu.vn |
| 2 | 24520247 | Nguyễn Lê Bảo Đan | https://github.com/bdan0412 | 24520247@gm.uit.edu.vn |
| 3 | 24520479 | Phan Thế Hiển | https://github.com/eltuzk | 24520479@gm.uit.edu.vn |
| 4 | 24520304 | Đào Trọng Định | https://github.com/dinh404 | 24520304@gm.uit.edu.vn |
| 5 | 24520678 | Nguyễn Hoàng Quốc Huy | https://github.com/nguyenhoangquochuyqnm-sketch | 24520678@gm.uit.edu.vn |