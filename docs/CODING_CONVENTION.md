# Coding Convention & Architecture Guide

# Healthcare Management System

> Tài liệu này là chuẩn bắt buộc cho toàn bộ thành viên trong nhóm.
> Mọi code được push lên Git phải tuân theo quy ước này.

---

## 1. Kiến trúc 5 Layer (Bắt buộc)

```
Controller
    ↕ DTO (Request / Response)
Service Interface
Service Impl
    ↕ Mapper (Thủ công)
Repository
Entity
```

Mỗi tầng có trách nhiệm rõ ràng:

| Layer                 | Trách nhiệm                                                                                  |
| --------------------- | -------------------------------------------------------------------------------------------- |
| **Controller**        | Nhận HTTP request, validate đầu vào cơ bản, trả về HTTP response. Không chứa business logic. |
| **Service Interface** | Định nghĩa contract (các method cần implement).                                              |
| **Service Impl**      | Chứa toàn bộ business logic, gọi Repository, dùng Mapper để convert.                         |
| **Repository**        | Tương tác với DB qua JPA/Hibernate. Chỉ chứa query, không có logic.                          |
| **Entity**            | Ánh xạ trực tiếp với bảng trong DB. Không expose ra ngoài Controller.                        |
| **DTO**               | Đối tượng truyền dữ liệu giữa Client ↔ Controller ↔ Service. Tách biệt hoàn toàn với Entity. |
| **Mapper**            | Convert thủ công Entity ↔ DTO. Không dùng MapStruct.           |

---

## 2. Cấu trúc Package

```
com.project.healthcare
├── controller/
│   ├── AuthController.java
│   ├── RoleController.java
│   └── ...
├── service/
│   ├── RoleService.java              ← Interface
│   └── impl/
│       └── RoleServiceImpl.java      ← Implementation
├── repository/
│   ├── RoleRepository.java
│   └── ...
├── entity/
│   ├── Role.java
│   └── ...
├── dto/
│   ├── request/
│   │   ├── RoleRequest.java
│   │   └── ...
│   └── response/
│       ├── RoleResponse.java
│       └── ...
├── mapper/
│   ├── RoleMapper.java
│   └── ...
├── security/
│   ├── JwtService.java
│   ├── JwtAuthFilter.java
│   └── SecurityConfig.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    └── ...
```

---

## 3. Quy ước đặt tên

### 3.0 Bảng áp dụng nhanh kiểu đặt tên

| Đối tượng                   | Kiểu đặt tên       | Ví dụ                            |
| --------------------------- | ------------------ | -------------------------------- |
| Class, Interface, Enum, DTO | `PascalCase`       | `DoctorSchedule`, `RoleResponse` |
| Method, field, biến Java    | `camelCase`        | `doctorId`, `createDoctor()`     |
| Hằng số (`static final`)    | `UPPER_SNAKE_CASE` | `MAX_LOGIN_ATTEMPTS`             |
| Tên bảng, tên cột DB        | `snake_case`       | `doctor_schedule`, `doctor_id`   |
| URL endpoint REST           | `kebab-case`       | `/api/doctor-schedules`          |
| Package Java                | `lowercase`        | `com.healthcare.backend.service` |

### 3.1 Class

| Loại                | Pattern               | Ví dụ                                  |
| ------------------- | --------------------- | -------------------------------------- |
| Entity              | `PascalCase`          | `DoctorSchedule`, `MedicalRecord`      |
| Controller          | `{Entity}Controller`  | `DoctorController`                     |
| Service (Interface) | `{Entity}Service`     | `DoctorService`                        |
| Service (Impl)      | `{Entity}ServiceImpl` | `DoctorServiceImpl`                    |
| Repository          | `{Entity}Repository`  | `DoctorRepository`                     |
| DTO Request         | `{Entity}Request`     | `DoctorRequest`, `DoctorUpdateRequest` |
| DTO Response        | `{Entity}Response`    | `DoctorResponse`                       |
| Mapper              | `{Entity}Mapper`      | `DoctorMapper`                         |

### 3.2 Method trong Service

| Thao tác      | Tên method                             |
| ------------- | -------------------------------------- |
| Lấy danh sách | `getAll{Entity}s()`                    |
| Lấy theo ID   | `get{Entity}ById(Long id)`             |
| Tạo mới       | `create{Entity}(Request dto)`          |
| Cập nhật      | `update{Entity}(Long id, Request dto)` |
| Soft delete   | `deactivate{Entity}(Long id)`          |
| Xóa thật      | `delete{Entity}(Long id)` ← ít dùng    |

### 3.3 Endpoint URL

- Dùng **kebab-case**: `/api/doctor-schedules`, `/api/lab-test-requests`
- Dùng **số nhiều**: `/api/doctors`, không phải `/api/doctor`
- Hành động đặc biệt dùng suffix: `/api/appointments/{id}/cancel`

### 3.4 Biến và field

- **camelCase** cho biến Java: `doctorId`, `fullName`, `isActive`
- **snake_case** cho cột DB (Oracle): `doctor_id`, `full_name`, `is_active`
- Dùng annotation `@Column(name = "full_name")` để ánh xạ

---
