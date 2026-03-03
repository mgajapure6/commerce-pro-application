// UserDetailDTO.java
package com.commerce_pro_backend.user_identity.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDetailDTO extends UserDTO {
    private List<RoleAssignmentDTO> roleAssignments;
    private List<AuditLogDTO> recentActivity;
    private String createdBy;
    private boolean mustChangePassword;
    private int failedLoginAttempts;
}