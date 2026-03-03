package com.commerce_pro_backend.user_identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
    
    private String mfaCode;
}