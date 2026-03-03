package com.commerce_pro_backend.user_identity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private LocalDateTime issuedAt;
    private String userId;
    private String username;
    private boolean superAdmin;
}