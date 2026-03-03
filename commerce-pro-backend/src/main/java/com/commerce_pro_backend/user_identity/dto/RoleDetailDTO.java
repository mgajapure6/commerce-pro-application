package com.commerce_pro_backend.user_identity.dto;

import java.util.List;
import java.util.Set;

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
public class RoleDetailDTO extends RoleDTO {
    private Set<PermissionDTO> permissions;
    private List<RoleDTO> childRoles;
    private List<String> constraints;
    private boolean requiresMfa;
    private Integer sessionTimeoutMinutes;
    private String allowedIpPatterns;
}