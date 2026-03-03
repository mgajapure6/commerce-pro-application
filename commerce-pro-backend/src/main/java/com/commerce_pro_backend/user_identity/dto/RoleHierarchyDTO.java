// RoleHierarchyDTO.java
package com.commerce_pro_backend.user_identity.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoleHierarchyDTO {
    private String roleId;
    private String roleCode;
    private String roleName;
    private String parentRoleId;
    private List<RoleHierarchyDTO> children;
    private int depth;
}