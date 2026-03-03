// RoleDTO.java
package com.commerce_pro_backend.user_identity.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDTO {
    private String id;
    private String code;
    private String name;
    private String description;
    private String type;
    private boolean system;
    private boolean superAdmin;
    private String parentRoleId;
    private String parentRoleName;
    private int permissionCount;
    private int assignedUserCount;
    private LocalDateTime createdAt;
}