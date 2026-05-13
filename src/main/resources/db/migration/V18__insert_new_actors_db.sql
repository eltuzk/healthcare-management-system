-- ==========================================
-- BẢNG LỄ TÂN (RECEPTIONIST)
-- ==========================================
CREATE TABLE RECEPTIONIST (
    receptionist_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id      NUMBER        NOT NULL UNIQUE,
    full_name       VARCHAR2(200) NOT NULL,
    identity_num    VARCHAR2(50)  UNIQUE,
    gender          VARCHAR2(10),
    phone           VARCHAR2(20),
    address         VARCHAR2(500),
    date_of_birth   DATE,
    hire_date       DATE,
    shift           VARCHAR2(50), -- Ca làm việc (VD: Morning, Evening, Night)
    is_active       NUMBER(1) DEFAULT 1 NOT NULL,
    CONSTRAINT fk_receptionist_account FOREIGN KEY (account_id) REFERENCES ACCOUNT(account_id),
    CONSTRAINT chk_receptionist_active CHECK (is_active IN (0, 1)),
    CONSTRAINT chk_receptionist_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);

-- ==========================================
-- BẢNG KỸ THUẬT VIÊN (TECHNICIAN)
-- Ví dụ: KTV xét nghiệm, KTV X-Quang,...
-- ==========================================
CREATE TABLE TECHNICIAN (
    technician_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id      NUMBER        NOT NULL UNIQUE,
    full_name       VARCHAR2(200) NOT NULL,
    qualification   VARCHAR2(200),
    specialty_area  VARCHAR2(200), -- Chuyên môn/Phòng ban (VD: Radiology, Laboratory)
    license_num     VARCHAR2(100) UNIQUE, -- Chứng chỉ hành nghề (nếu có)
    identity_num    VARCHAR2(50)  UNIQUE,
    gender          VARCHAR2(10),
    phone           VARCHAR2(20),
    address         VARCHAR2(500),
    date_of_birth   DATE,
    hire_date       DATE,
    experience      NUMBER(3),
    is_active       NUMBER(1) DEFAULT 1 NOT NULL,
    CONSTRAINT fk_technician_account FOREIGN KEY (account_id) REFERENCES ACCOUNT(account_id),
    CONSTRAINT chk_technician_active CHECK (is_active IN (0, 1)),
    CONSTRAINT chk_technician_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);

-- ==========================================
-- BẢNG DƯỢC SĨ (PHARMACIST)
-- ==========================================
CREATE TABLE PHARMACIST (
    pharmacist_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id      NUMBER        NOT NULL UNIQUE,
    full_name       VARCHAR2(200) NOT NULL,
    qualification   VARCHAR2(200),
    license_num     VARCHAR2(100) NOT NULL UNIQUE, -- Chứng chỉ hành nghề Dược
    identity_num    VARCHAR2(50)  UNIQUE,
    gender          VARCHAR2(10),
    phone           VARCHAR2(20),
    address         VARCHAR2(500),
    date_of_birth   DATE,
    hire_date       DATE,
    experience      NUMBER(3),
    is_active       NUMBER(1) DEFAULT 1 NOT NULL,
    CONSTRAINT fk_pharmacist_account FOREIGN KEY (account_id) REFERENCES ACCOUNT(account_id),
    CONSTRAINT chk_pharmacist_active CHECK (is_active IN (0, 1)),
    CONSTRAINT chk_pharmacist_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);

-- ==========================================
-- BẢNG KẾ TOÁN (ACCOUNTANT)
-- ==========================================
CREATE TABLE ACCOUNTANT (
    accountant_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id      NUMBER        NOT NULL UNIQUE,
    full_name       VARCHAR2(200) NOT NULL,
    qualification   VARCHAR2(200), -- Bằng cấp, chứng chỉ (VD: CPA, ACCA)
    identity_num    VARCHAR2(50)  UNIQUE,
    gender          VARCHAR2(10),
    phone           VARCHAR2(20),
    address         VARCHAR2(500),
    date_of_birth   DATE,
    hire_date       DATE,
    experience      NUMBER(3),
    is_active       NUMBER(1) DEFAULT 1 NOT NULL,
    CONSTRAINT fk_accountant_account FOREIGN KEY (account_id) REFERENCES ACCOUNT(account_id),
    CONSTRAINT chk_accountant_active CHECK (is_active IN (0, 1)),
    CONSTRAINT chk_accountant_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);