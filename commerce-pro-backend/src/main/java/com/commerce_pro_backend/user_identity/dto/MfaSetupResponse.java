package com.commerce_pro_backend.user_identity.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MfaSetupResponse {
    private String secret;
    private String otpauthUrl;
    private String issuer;
    private String accountName;
}
