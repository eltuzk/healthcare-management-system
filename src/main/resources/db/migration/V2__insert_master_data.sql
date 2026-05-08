-- Insert role mặc định
INSERT INTO ROLE (role_name, description) VALUES ('ADMIN', 'Quản trị viên hệ thống');
INSERT INTO ROLE (role_name, description) VALUES ('DOCTOR', 'Bác sĩ');
INSERT INTO ROLE (role_name, description) VALUES ('PATIENT', 'Bệnh nhân');
INSERT INTO ROLE (role_name, description) VALUES ('RECEPTIONIST', 'Lễ tân');
INSERT INTO ROLE (role_name, description) VALUES ('TECHNICIAN', 'Kỹ thuật viên');
INSERT INTO ROLE (role_name, description) VALUES ('PHARMACIST', 'Dược sĩ');
INSERT INTO ROLE (role_name, description) VALUES ('ACCOUNTANT', 'Kế toán');

-- Insert account admin
INSERT INTO ACCOUNT (email, password_hash, role_id, is_active)
VALUES (
           'admin@gmail.com',
           'admin123',
           (SELECT role_id FROM ROLE WHERE role_name = 'ADMIN'),
           1
       );

COMMIT;