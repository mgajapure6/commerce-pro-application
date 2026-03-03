// ImpersonationToken.java
package com.commerce_pro_backend.user_identity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ImpersonationToken {
    private String token;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private String targetUserId;
    private String targetUsername;
    private String originalAdminId;
}