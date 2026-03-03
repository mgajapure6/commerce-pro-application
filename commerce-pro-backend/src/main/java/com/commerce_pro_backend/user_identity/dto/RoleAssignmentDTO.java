// RoleAssignmentDTO.java
package com.commerce_pro_backend.user_identity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoleAssignmentDTO {
    private String assignmentId;
    private String roleId;
    private String roleCode;
    private String roleName;
    private String status;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String assignedBy;
    private LocalDateTime assignedAt;
    private String scopeContext;
}