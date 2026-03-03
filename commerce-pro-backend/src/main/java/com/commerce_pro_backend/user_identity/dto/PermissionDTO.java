// PermissionDTO.java
package com.commerce_pro_backend.user_identity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class PermissionDTO {
    private String code;
    private String name;
    private String description;
    private String category;
    private boolean system;
    private boolean requiresApproval;
    private int riskLevel;
    private Set<String> applicableScopes;
    private int roleCount;
    private LocalDateTime createdAt;
}