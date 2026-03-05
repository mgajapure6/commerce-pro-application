package com.commerce_pro_backend.user_identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MfaVerificationRequest {
    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "MFA code must be a 6-digit number")
    private String code;
}
