// UpdatePermissionRequest.java
package com.commerce_pro_backend.user_identity.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePermissionRequest {
    private String name;
    private String description;
    private Boolean requiresApproval;
    private Integer riskLevel;
    private List<String> applicableScopes;
}