package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmManualPaymentRequest {

    @NotBlank(message = "Receipt number is required")
    @Size(max = 100, message = "Receipt number must not exceed 100 characters")
    private String receiptNumber;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;
}
