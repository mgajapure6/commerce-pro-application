package com.commerce_pro_backend.user_identity.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank @Size(max = 50)
    private String code;
    
    @NotBlank
    private String name;
    private String description;
    private String parentRoleId;
    private List<String> permissionCodes;
    private boolean requiresMfa = false;
    private Integer sessionTimeoutMinutes;
    private String allowedIpPatterns;
}
