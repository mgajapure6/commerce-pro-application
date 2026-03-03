package com.commerce_pro_backend.user_identity.dto;

import java.util.List;

import com.commerce_pro_backend.user_identity.enums.PermissionCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePermissionRequest {
    @NotBlank @Pattern(regexp = "^[a-z]+:[a-z]+:[a-z]+$")
    private String code;
    
    @NotBlank
    private String name;
    private String description;
    private PermissionCategory category;
    private Integer riskLevel = 1;
    private boolean requiresApproval = false;
    private List<String> applicableScopes;
}
