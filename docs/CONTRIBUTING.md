# Contributing Guide

## Git Workflow

### Branch Strategy

```
main              ← Production (protected)
  │
develop           ← Integration branch (protected)
  │
feature/*         ← New features
bugfix/*          ← Bug fixes
hotfix/*          ← Urgent fixes for production
```

### Quy trình làm việc

```bash
# 1. Clone repo (lần đầu)
git clone <repo-url>
cd healthcare-backend

# 2. Luôn bắt đầu từ develop mới nhất
git checkout develop
git pull origin develop

# 3. Tạo branch mới
git checkout -b feature/ten-tinh-nang
# hoặc: git checkout -b bugfix/ten-loi

# 4. Code và commit thường xuyên
git add .
git commit -m "feat: add appointment api endpoint"

# 5. Push branch lên remote
git push origin feature/ten-tinh-nang

# 6. Tạo Pull Request trên GitHub
#    - Base: develop
#    - Compare: feature/ten-tinh-nang

# 7. Sau khi PR được merge, xóa branch local
git checkout develop
git pull origin develop
git branch -d feature/ten-tinh-nang
```

### Commit Message Convention

Format: `type: message`

| Type | Mô tả |
|------|-------|
| `feat` | Tính năng mới |
| `fix` | Sửa lỗi |
| `refactor` | Refactor code |
| `docs` | Documentation |
| `test` | Thêm/sửa tests |
| `chore` | Config, dependencies, workflow |

Ví dụ:
```
feat: add doctor schedule endpoint
fix: resolve patient record update bug
docs: update API documentation for appointments
```

### Code Review

- Mỗi PR cần ít nhất 1 approval
- Resolve tất cả comments trước khi merge
- Đảm bảo các test case (nếu có) chạy thành công không có lỗi.

## Getting Started

### Yêu cầu hệ thống

- Java 21
- Maven (hoặc sử dụng Maven wrapper `./mvnw` có sẵn trong dự án)
- Database: Oracle Database
- IDE khuyên dùng: IntelliJ IDEA, hoặc VS Code

### Chạy dự án

```bash
# 1. Tải dependencies và build project (bỏ qua tests)
./mvnw clean install -DskipTests

# 2. Chạy ứng dụng (Development)
./mvnw spring-boot:run

# Server mặc định sẽ chạy ở http://localhost:8080
```

## Project Structure

Dự án phát triển dựa trên Spring Boot và tuân theo cấu trúc chuẩn:

```
healthcare-backend/
├── src/
│   ├── main/
│   │   ├── java/com/healthcare/backend/
│   │   │   ├── controllers/  # Các REST API controllers xử lý request
│   │   │   ├── services/     # Các classes chứa core business logic (Interface & Impl)
│   │   │   ├── repositories/ # Các interface giao tiếp với DB (thường dùng Spring Data JPA)
│   │   │   ├── models/       # Entity classes mapping với Database tables
│   │   │   ├── dtos/         # Data Transfer Objects (Request và Response objects)
│   │   │   ├── exceptions/   # Customized exceptions và Global Error Handler
│   │   │   ├── security/     # Cấu hình bảo mật, authentication & authorization
│   │   │   └── config/       # Các file cấu hình chung của Spring (CORS, Bean configs, ...)
│   │   └── resources/
│   │       ├── application.properties # File cấu hình chung của dự án
│   │       └── static/ & templates/
│   └── test/                 # Chứa các Unit và Integration test cases
├── .mvn/                     # Thư mục cấu hình cho Maven Wrapper
└── pom.xml                   # File quản lý project metadata và dependencies
```

## Commands Reference

| Lệnh | Phím tắt tương ứng |
|---------|-------------|
| `./mvnw clean install -DskipTests` | Build mã nguồn thành file `.jar` bỏ qua test |
| `./mvnw spring-boot:run` | Start local backend server |
| `./mvnw test` | Chạy toàn bộ tests |
| `./mvnw clean` | Dọn dẹp các artifact cũ (xóa mục `target/`) |
| `./mvnw dependency:tree` | Kiểm tra cây thư viện (xem xung đột gói) |
