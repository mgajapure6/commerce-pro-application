// UpdateRoleRequest.java
package com.commerce_pro_backend.user_identity.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRoleRequest {
    private String name;
    private String description;
    private Boolean requiresMfa;
    private Integer sessionTimeoutMinutes;
    private String allowedIpPatterns;
    private List<String> constraints;
}