-- ==========================================
-- BẢNG QUẢN TRỊ VIÊN (ADMINISTRATOR)
-- ==========================================
CREATE TABLE ADMINISTRATOR (
    administrator_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id      NUMBER        NOT NULL UNIQUE,
    full_name       VARCHAR2(200) NOT NULL,
    identity_num    VARCHAR2(50)  UNIQUE,
    gender          VARCHAR2(10),
    phone           VARCHAR2(20),
    address         VARCHAR2(500),
    date_of_birth   DATE,
    hire_date       DATE,
    is_active       NUMBER(1) DEFAULT 1 NOT NULL,
    CONSTRAINT fk_admin_account FOREIGN KEY (account_id) REFERENCES ACCOUNT(account_id),
    CONSTRAINT chk_admin_active CHECK (is_active IN (0, 1)),
    CONSTRAINT chk_admin_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);
