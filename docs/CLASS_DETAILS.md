# Từ Điển Sơ Đồ Lớp (Class Dictionary) - Healthcare Management System

Tài liệu này liệt kê chi tiết 100% các Lớp (Classes), Thuộc tính (Attributes) được trích xuất từ Java Entities, và Phương thức (Methods) được trích xuất từ các interface Service của Spring Boot. Bạn có thể sử dụng file này làm tài liệu tham khảo chính xác nhất để tự vẽ UML Class Diagram.

## 📌 Account

### Thuộc tính (Attributes):
- `+Long accountId`
- `+String email`
- `+String passwordHash`
- `+String googleId`
- `+Role role`
- `+Integer isActive`

### Phương thức (Methods):
- `+getAll(Pageable pageable): Page<AccountResponse>`
- `+getById(Long id): AccountResponse`
- `+create(AccountRequest request): AccountResponse`
- `+update(Long id, AccountRequest request): AccountResponse`
- `+delete(Long id): void`
- `+getMe(String email): AccountResponse`
- `+register(RegisterRequest request): RegisterResponse`
- `+verifyEmail(String token): void`
- `+login(AuthRequest request): AuthResponse`
- `+loginWithGoogle(GoogleLoginRequest request): AuthResponse`
- `+processForgotPassword(ForgotPasswordRequest request): void`
- `+executeResetPassword(String token, ResetPasswordRequest request): void`
- `+changePassword(String email, ChangePasswordRequest request): void`

---

## 📌 AccountPermission

### Thuộc tính (Attributes):
- `+AccountPermissionId accountPermissionId`
- `+Account account`
- `+Permission permission`

### Phương thức (Methods):
- `+assign(AccountPermissionRequest request): AccountPermissionResponse`
- `+revoke(AccountPermissionRequest request): void`
- `+getByAccountId(Long accountId): List<AccountPermissionResponse>`

---

## 📌 AccountPermissionId

### Thuộc tính (Attributes):
- `+Long accountId`
- `+Long permissionId`

### Phương thức (Methods):
- (Lớp này không có phương thức Service riêng hoặc chỉ dùng CRUD ngầm định)

---

## 📌 Accountant

### Thuộc tính (Attributes):
- `+Long accountantId`
- `+Account account`
- `+String fullName`
- `+String qualification`
- `+String identityNum`
- `+String gender`
- `+String phone`
- `+String address`
- `+LocalDate dateOfBirth`
- `+LocalDate hireDate`
- `+Integer experience`
- `+Integer isActive`

### Phương thức (Methods):
- `+getMe(String email): AccountantResponse`
- `+updateMe(String email, AccountantRequest request): AccountantResponse`
- `+getAll(Pageable pageable): Page<AccountantResponse>`
- `+getById(Long id): AccountantResponse`
- `+update(Long id, AccountantRequest request): AccountantResponse`
- `+delete(Long id): void`

---

## 📌 Administrator

### Thuộc tính (Attributes):
- `+Long administratorId`
- `+Account account`
- `+String fullName`
- `+String identityNum`
- `+String gender`
- `+String phone`
- `+String address`
- `+LocalDate dateOfBirth`
- `+LocalDate hireDate`
- `+Integer isActive`

### Phương thức (Methods):
- `+getAll(Pageable pageable): Page<AdministratorResponse>`
- `+getById(Long adminId): AdministratorResponse`
- `+getMe(String email): AdministratorResponse`
- `+updateMe(String email, AdministratorRequest request): AdministratorResponse`
- `+create(AdministratorRequest request): AdministratorResponse`
- `+update(Long adminId, AdministratorRequest request): AdministratorResponse`
- `+delete(Long adminId): void`

---

## 📌 AdmissionRecord

### Thuộc tính (Attributes):
- `+Long admissionRecordId`
- `+AdmissionRequest admissionRequest`
- `+String bloodPressure`
- `+Integer heartRate`
- `+Double temperature`
- `+LocalDateTime recordDate`

### Phương thức (Methods):
- (Lớp này không có phương thức Service riêng hoặc chỉ dùng CRUD ngầm định)

---

## 📌 AdmissionRequest

### Thuộc tính (Attributes):
- `+Long admissionId`
- `+Patient patient`
- `+MedicalRecord medicalRecord`
- `+Bed bed`
- `+LocalDate admissionDate`
- `+LocalDate dischargeDate`
- `+AdmissionStatus status`
- `+BigDecimal totalPrice`

### Phương thức (Methods):
- (Lớp này không có phương thức Service riêng hoặc chỉ dùng CRUD ngầm định)

---

## 📌 Appointment

### Thuộc tính (Attributes):
- `+Long appointmentId`
- `+Patient patient`
- `+DoctorSchedule doctorSchedule`
- `+ConsultationFee consultationFee`
- `+PaymentRecord paymentRecord`
- `+Integer queueNum`
- `+String appointmentCode`
- `+AppointmentStatus status`
- `+String initialSymptoms`
- `+String visitReason`
- `+String feeNameSnapshot`
- `+LocalDateTime createdAt`
- `+LocalDateTime updatedAt`
- `+LocalDateTime paidAt`
- `+LocalDateTime paymentExpiresAt`
- `+Long sepayTransactionId`
- `+String paymentReferenceCode`
- `+String paymentContent`
- `+LocalDateTime checkedInAt`
- `+LocalDateTime cancelledAt`
- `+Long versionNumber`

### Phương thức (Methods):
- `+create(CreateAppointmentRequest request): AppointmentResponse`
- `+createWalkInPaidAppointment(CreateWalkInAppointmentRequest request): AppointmentResponse`
- `+getById(Long appointmentId): AppointmentResponse`
- `+getAll(Long patientId, Long doctorId, Long doctorScheduleId, AppointmentStatus status): List<AppointmentResponse>`
- `+checkIn(Long appointmentId): AppointmentResponse`
- `+start(Long appointmentId): AppointmentResponse`
- `+cancel(Long appointmentId): AppointmentResponse`
- `+confirmPaymentFromSepayWebhook(SepayWebhookRequest request, String secretKeyHeader): AppointmentResponse`
- `+expirePendingPaymentReservations(): void`

---

## 📌 Bed

### Thuộc tính (Attributes):
- `+Long bedId`
- `+BigDecimal price`
- `+BedStatus status`
- `+Room room`

### Phương thức (Methods):
- `+getBedsByRoom(Long roomId): List<BedResponse>`
- `+addBed(Long roomId, BedRequest request): BedResponse`
- `+updateBed(Long bedId, BedRequest request): BedResponse`
- `+deleteBed(Long bedId): void`

---

## 📌 Branch

### Thuộc tính (Attributes):
- `+Long branchId`
- `+String branchName`
- `+String branchAddress`
- `+String branchHotline`

### Phương thức (Methods):
- `+getAll(): List<BranchResponse>`
- `+getById(Long id): BranchResponse`
- `+create(BranchRequest request): BranchResponse`
- `+update(Long id, BranchRequest request): BranchResponse`
- `+delete(Long id): void`

---

## 📌 ConsultationFee

### Thuộc tính (Attributes):
- `+Long feeId`
- `+String feeCode`
- `+String feeName`
- `+String specialty`
- `+Specialty specialtyRef`
- `+BigDecimal price`
- `+Integer isActive`
- `+LocalDateTime createdAt`
- `+LocalDateTime updatedAt`

### Phương thức (Methods):
- `+create(ConsultationFeeRequest request): ConsultationFeeResponse`
- `+getAll(): List<ConsultationFeeResponse>`
- `+getById(Long feeId): ConsultationFeeResponse`
- `+update(Long feeId, ConsultationFeeRequest request): ConsultationFeeResponse`
- `+deactivate(Long feeId): ConsultationFeeResponse`

---

## 📌 Doctor

### Thuộc tính (Attributes):
- `+Long doctorId`
- `+Account account`
- `+String fullName`
- `+String qualification`
- `+String specialization`
- `+Specialty specialty`
- `+String licenseNum`
- `+String identityNum`
- `+String gender`
- `+String phone`
- `+String address`
- `+LocalDate dateOfBirth`
- `+LocalDate hireDate`
- `+String experience`
- `+Integer isActive`

### Phương thức (Methods):
- `+getAll(Pageable pageable): Page<DoctorResponse>`
- `+getById(Long doctorId): DoctorResponse`
- `+getMe(String email): DoctorResponse`
- `+updateMe(String email, DoctorRequest request): DoctorResponse`
- `+create(DoctorRequest request): DoctorResponse`
- `+update(Long doctorId, DoctorRequest request): DoctorResponse`
- `+delete(Long doctorId): void`
- `+isActive(): boolean`

---

## 📌 DoctorSchedule

### Thuộc tính (Attributes):
- `+Long doctorScheduleId`
- `+Doctor doctor`
- `+Room room`
- `+LocalDate scheduleDate`
- `+ShiftType shift`
- `+Integer maxCapacity`
- `+Integer currentBookingCount`
- `+Integer lastQueueNumber`
- `+String note`
- `+LocalDateTime createdAt`
- `+LocalDateTime updatedAt`
- `+Long versionNumber`

### Phương thức (Methods):
- `+create(CreateDoctorScheduleRequest request): DoctorScheduleResponse`
- `+importSchedules(MultipartFile file): DoctorScheduleImportResponse`
- `+getAll(LocalDate date, LocalDate startDate, LocalDate endDate, Long doctorId, Long roomId): List<DoctorScheduleResponse>`
- `+getById(Long doctorScheduleId): DoctorScheduleResponse`
- `+update(Long doctorScheduleId, UpdateDoctorScheduleRequest request): DoctorScheduleResponse`
- `+delete(Long doctorScheduleId): void`

---

## 📌 LabTest

### Thuộc tính (Attributes):
- `+Long labTestId`
- `+String labTestName`
- `+BigDecimal price`
- `+Integer isActive`

### Phương thức (Methods):
- `+getAllLabTests(): List<LabTestResponse>`
- `+getLabTestById(Long id): LabTestResponse`
- `+createLabTest(LabTestRequest request): LabTestResponse`
- `+updateLabTest(Long id, LabTestRequest request): LabTestResponse`
- `+deactivateLabTest(Long id): LabTestResponse`

---

## 📌 LabTestRequest

### Thuộc tính (Attributes):
- `+Long labTestRequestId`
- `+MedicalRecord medRecord`
- `+String requestCode`
- `+LabTestRequestStatus status`
- `+PaymentStatus paymentStatus`
- `+BigDecimal totalPrice`
- `+String note`
- `+LocalDateTime createdAt`
- `+LocalDateTime updatedAt`
- `+LocalDateTime paidAt`
- `+List<LabTestRequestItem> items`

### Phương thức (Methods):
- `+createRequest(CreateLabTestRequestRequest request): LabTestRequestResponse`
- `+getRequestById(Long id): LabTestRequestResponse`
- `+getRequests(Long medRecordId, LabTestRequestStatus status, Pageable pageable): Page<LabTestRequestResponse>`
- `+getRequestsByMedRecord(Long medRecordId): List<LabTestRequestResponse>`
- `+updateStatus(Long id, UpdateLabTestRequestStatusRequest request): LabTestRequestResponse`
- `+createResult(Long requestId, CreateLabTestResultRequest request): LabTestResultResponse`
- `+updateResult(Long resultId, UpdateLabTestResultRequest request): LabTestResultResponse`
- `+getResultByRequestId(Long requestId): LabTestResultResponse`

---

## 📌 LabTestRequestItem

### Thuộc tính (Attributes):
- `+LabTestRequestItemId id`
- `+LabTestRequest labTestRequest`
- `+LabTest labTest`
- `+BigDecimal snapshotPrice`

### Phương thức (Methods):
- (Lớp này không có phương thức Service riêng hoặc chỉ dùng CRUD ngầm định)

---

## 📌 LabTestRequestItemId

### Thuộc tính (Attributes):
- `+Long labTestRequestId`
- `+Long labTestId`

### Phương thức (Methods):
- (Lớp này không có phương thức Service riêng hoặc chỉ dùng CRUD ngầm định)

---

## 📌 LabTestResult

### Thuộc tính (Attributes):
- `+Long labTestResultId`
- `+LabTestRequest labTestRequest`
- `+String resultData`
- `+LocalDateTime resultDate`

### Phương thức (Methods):
- (Lớp này không có phương thức Service riêng hoặc chỉ dùng CRUD ngầm định)

---

## 📌 MedicalRecord

### Thuộc tính (Attributes):
- `+Long medicalRecordId`
- `+Appointment appointment`
- `+Doctor doctor`
- `+Patient patient`
- `+String initialDiagnosis`
- `+String clinicalConclusion`
- `+MedicalRecordConclusionType conclusionType`
- `+String clinicalNotes`
- `+String treatmentPlan`
- `+MedicalRecordStatus status`
- `+BigDecimal totalPrice`
- `+LocalDateTime createdAt`
- `+LocalDateTime updatedAt`
- `+LocalDateTime completedAt`
- `+LocalDateTime lockedAt`
- `+Long versionNumber`

### Phương thức (Methods):
- `+initializePaymentRecord(MedicalRecord medicalRecord): void`
- `+syncBilling(Long medicalRecordId): void`
- `+createFromAppointment(Long appointmentId, CreateMedicalRecordRequest request): MedicalRecordResponse`
- `+getById(Long medicalRecordId): MedicalRecordResponse`
- `+getAll(Long patientId, Long doctorId, MedicalRecordStatus status, LocalDate date): List<MedicalRecordResponse>`
- `+update(Long medicalRecordId, UpdateMedicalRecordRequest request): MedicalRecordResponse`
- `+complete(Long medicalRecordId): MedicalRecordResponse`
- `+lock(Long medicalRecordId): MedicalRecordResponse`
- `+validateEligibleForAdmission(Long medicalRecordId): void`
- `+validateCanCreateRequest(MedicalRecord medicalRecord): void`
- `+validateCanUpdateRequest(MedicalRecord medicalRecord): void`
- `+validateReadyToComplete(MedicalRecord medicalRecord): void`
- `+completeIfReady(Long medicalRecordId): void`
- `+prePersist(): void`
- `+preUpdate(): void`

---

## 📌 MedicalService

### Thuộc tính (Attributes):
- `+Long medServiceId`
- `+String medicalServiceName`
- `+BigDecimal price`
- `+Integer isActive`

### Phương thức (Methods):
- `+getAllMedicalServices(): List<MedicalServiceResponse>`
- `+getMedicalServiceById(Long id): MedicalServiceResponse`
- `+createMedicalService(MedicalServiceRequest request): MedicalServiceResponse`
- `+updateMedicalService(Long id, MedicalServiceRequest request): MedicalServiceResponse`
- `+deactivateMedicalService(Long id): MedicalServiceResponse`

---

## 📌 MedicalServiceRequest

### Thuộc tính (Attributes):
- `+Long medServiceRequestId`
- `+MedicalRecord medRecord`
- `+String requestCode`
- `+MedicalServiceRequestStatus status`
- `+PaymentStatus paymentStatus`
- `+BigDecimal totalPrice`
- `+String currency`
- `+String note`
- `+LocalDateTime createdAt`
- `+LocalDateTime updatedAt`
- `+LocalDateTime cancelledAt`
- `+LocalDateTime paidAt`
- `+LocalDateTime confirmedAt`
- `+List<MedicalServiceRequestItem> items`

### Phương thức (Methods):
- `+createRequest(CreateMedicalServiceRequestRequest request): MedicalServiceRequestResponse`
- `+getRequestById(Long id): MedicalServiceRequestResponse`
- `+getRequests(Long medRecordId, MedicalServiceRequestStatus status, Pageable pageable): Page<MedicalServiceRequestResponse>`
- `+updateStatus(Long id, UpdateMedicalServiceRequestStatusRequest request): MedicalServiceRequestResponse`
- `+createResult(Long requestId, UpdateMedicalServiceResultRequest request): MedicalServiceResultResponse`
- `+updateResult(Long resultId, UpdateMedicalServiceResultRequest request): MedicalServiceResultResponse`
- `+getResultByRequestId(Long requestId): MedicalServiceResultResponse`

---

## 📌 MedicalServiceRequestItem

### Thuộc tính (Attributes):
- `+MedicalServiceRequestItemId id`
- `+MedicalServiceRequest medicalServiceRequest`
- `+MedicalService medicalService`
- `+BigDecimal snapshotPrice`

### Phương thức (Methods):
- (Lớp này không có phương thức Service riêng hoặc chỉ dùng CRUD ngầm định)

---

## 📌 MedicalServiceRequestItemId

### Thuộc tính (Attributes):
- `+Long medServiceRequestId`
- `+Long medServiceId`

### Phương thức (Methods):
- (Lớp này không có phương thức Service riêng hoặc chỉ dùng CRUD ngầm định)

---

## 📌 MedicalServiceResult

### Thuộc tính (Attributes):
- `+Long medServiceResultId`
- `+MedicalServiceRequest medicalServiceRequest`
- `+String resultData`
- `+LocalDateTime createdAt`

### Phương thức (Methods):
- (Lớp này không có phương thức Service riêng hoặc chỉ dùng CRUD ngầm định)

---

## 📌 Medicine

### Thuộc tính (Attributes):
- `+Long medicineId`
- `+String medicineName`
- `+String activeIngredient`
- `+String unit`
- `+String description`
- `+Integer isActive`

### Phương thức (Methods):
- `+getAllMedicines(): List<MedicineResponse>`
- `+getMedicineById(Long id): MedicineResponse`
- `+createMedicine(MedicineRequest request): MedicineResponse`
- `+updateMedicine(Long id, MedicineRequest request): MedicineResponse`
- `+deactivateMedicine(Long id): MedicineResponse`

---

## 📌 MedicineLot

### Thuộc tính (Attributes):
- `+Long medicineLotId`
- `+Medicine medicine`
- `+String lotNumber`
- `+LocalDate manufacturingDate`
- `+LocalDate expiryDate`
- `+Integer quantity`
- `+BigDecimal importPrice`
- `+Integer isActive`

### Phương thức (Methods):
- `+getAllMedicineLots(Long medicineId): List<MedicineLotResponse>`
- `+getMedicineLotById(Long id): MedicineLotResponse`
- `+createMedicineLot(MedicineLotRequest request): MedicineLotResponse`
- `+updateMedicineLot(Long id, MedicineLotRequest request): MedicineLotResponse`
- `+deactivateMedicineLot(Long id): MedicineLotResponse`

---

## 📌 Patient

### Thuộc tính (Attributes):
- `+Long patientId`
- `+Account account`
- `+String fullName`
- `+String gender`
- `+LocalDate dateOfBirth`
- `+String phone`
- `+String address`
- `+String identityNum`
- `+String medicalHistory`
- `+String allergy`
- `+Integer isActive`

### Phương thức (Methods):
- `+getAll(Pageable pageable): Page<PatientResponse>`
- `+getById(Long id): PatientResponse`
- `+create(PatientRequest request): PatientResponse`
- `+update(Long id, PatientRequest request): PatientResponse`
- `+delete(Long id): void`
- `+getMe(String email): PatientResponse`
- `+updateMe(String email, PatientRequest request): PatientResponse`
- `+search(String query): PatientResponse`

---

## 📌 PatientInsurance

### Thuộc tính (Attributes):
- `+Long patientInsuranceId`
- `+Patient patient`
- `+String insuranceNum`
- `+String status`
- `+LocalDate expiryDate`
- `+BigDecimal coveragePercent`

### Phương thức (Methods):
- `+getByPatientId(Long patientId): List<PatientInsuranceResponse>`
- `+create(PatientInsuranceRequest request): PatientInsuranceResponse`
- `+update(Long id, PatientInsuranceRequest request): PatientInsuranceResponse`
- `+delete(Long id): void`

---

## 📌 PaymentRecord

### Thuộc tính (Attributes):
- `+Long paymentRecordId`
- `+Appointment appointment`
- `+MedicalRecord medicalRecord`
- `+String requestCode`
- `+BigDecimal totalPrice`
- `+BigDecimal receivedAmount`
- `+PaymentStatus paymentStatus`
- `+LocalDateTime createdAt`
- `+LocalDateTime updatedAt`
- `+LocalDateTime paidAt`

### Phương thức (Methods):
- `+getAll(PaymentStatus paymentStatus, Long appointmentId, Long medicalRecordId): List<PaymentRecordResponse>`
- `+getById(Long paymentRecordId): PaymentRecordResponse`
- `+recordMedicalRecordCashPayment(Long medicalRecordId, RecordMedicalRecordPaymentRequest request): PaymentRecordResponse`

---

## 📌 PaymentTransaction

### Thuộc tính (Attributes):
- `+Long transactionId`
- `+PaymentRecord paymentRecord`
- `+String transferType`
- `+String gateway`
- `+String accountNumber`
- `+String sepayTransactionId`
- `+BigDecimal transferAmount`
- `+LocalDateTime transactionDate`
- `+String referenceCode`
- `+String content`
- `+String description`
- `+String receiptNumber`
- `+Account confirmedBy`
- `+PaymentTransactionStatus processStatus`
- `+String rawData`

### Phương thức (Methods):
- (Lớp này không có phương thức Service riêng hoặc chỉ dùng CRUD ngầm định)

---

## 📌 Permission

### Thuộc tính (Attributes):
- `+Long permissionId`
- `+String permissionName`
- `+String detail`

### Phương thức (Methods):
- `+getAll(): List<PermissionResponse>`
- `+getById(Long id): PermissionResponse`
- `+create(PermissionRequest request): PermissionResponse`
- `+update(Long id, PermissionRequest request): PermissionResponse`
- `+delete(Long id): void`

---

## 📌 Pharmacist

### Thuộc tính (Attributes):
- `+Long pharmacistId`
- `+Account account`
- `+String fullName`
- `+String qualification`
- `+String licenseNum`
- `+String identityNum`
- `+String gender`
- `+String phone`
- `+String address`
- `+LocalDate dateOfBirth`
- `+LocalDate hireDate`
- `+Integer experience`
- `+Integer isActive`

### Phương thức (Methods):
- `+getAll(Pageable pageable): Page<PharmacistResponse>`
- `+getById(Long id): PharmacistResponse`
- `+create(PharmacistRequest request): PharmacistResponse`
- `+update(Long id, PharmacistRequest request): PharmacistResponse`
- `+getMe(String email): PharmacistResponse`
- `+updateMe(String email, PharmacistRequest request): PharmacistResponse`
- `+delete(Long id): void`

---

## 📌 Prescription

### Thuộc tính (Attributes):
- `+Long prescriptionId`
- `+MedicalRecord medicalRecord`
- `+String note`
- `+Integer isActive`
- `+LocalDateTime createdAt`
- `+LocalDateTime updatedAt`
- `+List<PrescriptionDetail> prescriptionDetails`

### Phương thức (Methods):
- `+getAllPrescriptions(): List<PrescriptionResponse>`
- `+getPrescriptionById(Long id): PrescriptionResponse`
- `+getPrescriptionByMedicalRecordId(Long medicalRecordId): PrescriptionResponse`
- `+createPrescription(PrescriptionRequest request): PrescriptionResponse`
- `+updatePrescription(Long id, PrescriptionRequest request): PrescriptionResponse`
- `+deactivatePrescription(Long id): PrescriptionResponse`

---

## 📌 PrescriptionDetail

### Thuộc tính (Attributes):
- `+Long prescriptionDetailId`
- `+Prescription prescription`
- `+Medicine medicine`
- `+String dosage`
- `+String frequency`
- `+String duration`
- `+Integer quantity`
- `+String instruction`

### Phương thức (Methods):
- (Lớp này không có phương thức Service riêng hoặc chỉ dùng CRUD ngầm định)

---

## 📌 Receptionist

### Thuộc tính (Attributes):
- `+Long receptionistId`
- `+Account account`
- `+String fullName`
- `+String identityNum`
- `+String gender`
- `+String phone`
- `+String address`
- `+LocalDate dateOfBirth`
- `+LocalDate hireDate`
- `+String shift`
- `+Integer isActive`

### Phương thức (Methods):
- `+getAll(Pageable pageable): Page<ReceptionistResponse>`
- `+getById(Long id): ReceptionistResponse`
- `+create(ReceptionistRequest request): ReceptionistResponse`
- `+update(Long id, ReceptionistRequest request): ReceptionistResponse`
- `+getMe(String email): ReceptionistResponse`
- `+updateMe(String email, ReceptionistRequest request): ReceptionistResponse`
- `+delete(Long id): void`

---

## 📌 Role

### Thuộc tính (Attributes):
- `+Long roleId`
- `+String roleName`
- `+String description`

### Phương thức (Methods):
- `+getAll(): List<RoleResponse>`
- `+getById(Long id): RoleResponse`
- `+create(RoleRequest request): RoleResponse`
- `+update(Long id, RoleRequest request): RoleResponse`
- `+delete(Long id): void`

---

## 📌 RolePermission

### Thuộc tính (Attributes):
- `+RolePermissionId rolePermissionId`
- `+Role role`
- `+Permission permission`

### Phương thức (Methods):
- `+assign(RolePermissionRequest request): RolePermissionResponse`
- `+revoke(RolePermissionRequest request): void`
- `+getByRoleId(Long roleId): List<RolePermissionResponse>`

---

## 📌 RolePermissionId

### Thuộc tính (Attributes):
- `+Long roleId`
- `+Long permissionId`

### Phương thức (Methods):
- (Lớp này không có phương thức Service riêng hoặc chỉ dùng CRUD ngầm định)

---

## 📌 Room

### Thuộc tính (Attributes):
- `+Long roomId`
- `+String roomCode`
- `+String position`
- `+String note`
- `+Integer floor`
- `+RoomType roomType`
- `+Branch branch`
- `+Specialty specialty`
- `+List<Bed> beds`

### Phương thức (Methods):
- `+getAllRooms(Long roomTypeId): List<RoomResponse>`
- `+getRoomById(Long id): RoomResponse`
- `+createRoom(RoomRequest request): RoomResponse`
- `+updateRoom(Long id, RoomRequest request): RoomResponse`
- `+deleteRoom(Long id): void`

---

## 📌 RoomType

### Thuộc tính (Attributes):
- `+Long roomTypeId`
- `+String roomTypeName`

### Phương thức (Methods):
- `+getAllRoomTypes(): List<RoomTypeResponse>`
- `+getRoomTypeById(Long id): RoomTypeResponse`
- `+createRoomType(RoomTypeRequest request): RoomTypeResponse`
- `+updateRoomType(Long id, RoomTypeRequest request): RoomTypeResponse`
- `+deleteRoomType(Long id): void`

---

## 📌 Specialty

### Thuộc tính (Attributes):
- `+Long specialtyId`
- `+String specialtyCode`
- `+String specialtyName`
- `+Integer isActive`
- `+LocalDateTime createdAt`
- `+LocalDateTime updatedAt`

### Phương thức (Methods):
- `+create(SpecialtyRequest request): SpecialtyResponse`
- `+getAll(): List<SpecialtyResponse>`
- `+getById(Long specialtyId): SpecialtyResponse`
- `+update(Long specialtyId, SpecialtyRequest request): SpecialtyResponse`
- `+deactivate(Long specialtyId): SpecialtyResponse`

---

## 📌 Technician

### Thuộc tính (Attributes):
- `+Long technicianId`
- `+Account account`
- `+String fullName`
- `+String qualification`
- `+String specialtyArea`
- `+String licenseNum`
- `+String identityNum`
- `+String gender`
- `+String phone`
- `+String address`
- `+LocalDate dateOfBirth`
- `+LocalDate hireDate`
- `+Integer experience`
- `+Integer isActive`

### Phương thức (Methods):
- `+getAll(Pageable pageable): Page<TechnicianResponse>`
- `+getById(Long id): TechnicianResponse`
- `+create(TechnicianRequest request): TechnicianResponse`
- `+update(Long id, TechnicianRequest request): TechnicianResponse`
- `+getMe(String email): TechnicianResponse`
- `+updateMe(String email, TechnicianRequest request): TechnicianResponse`
- `+delete(Long id): void`

---

## 📊 Các Enum (Enumerations)

### `<<enumeration>> AppointmentStatus`
- (Các giá trị trạng thái đã được định nghĩa trong hệ thống)

### `<<enumeration>> MedicalRecordStatus`
- (Các giá trị trạng thái đã được định nghĩa trong hệ thống)

### `<<enumeration>> MedicalRecordConclusionType`
- (Các giá trị trạng thái đã được định nghĩa trong hệ thống)

### `<<enumeration>> PaymentStatus`
- (Các giá trị trạng thái đã được định nghĩa trong hệ thống)

### `<<enumeration>> PaymentTransactionStatus`
- (Các giá trị trạng thái đã được định nghĩa trong hệ thống)

### `<<enumeration>> RevenueOwnerType`
- (Các giá trị trạng thái đã được định nghĩa trong hệ thống)

### `<<enumeration>> LabTestRequestStatus`
- (Các giá trị trạng thái đã được định nghĩa trong hệ thống)

### `<<enumeration>> MedicalServiceRequestStatus`
- (Các giá trị trạng thái đã được định nghĩa trong hệ thống)

### `<<enumeration>> AdmissionStatus`
- (Các giá trị trạng thái đã được định nghĩa trong hệ thống)

### `<<enumeration>> BedStatus`
- (Các giá trị trạng thái đã được định nghĩa trong hệ thống)

### `<<enumeration>> ShiftType`
- (Các giá trị trạng thái đã được định nghĩa trong hệ thống)

