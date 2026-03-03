// RoleTemplateDTO.java
package com.commerce_pro_backend.user_identity.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoleTemplateDTO {
    private String templateCode;
    private String name;
    private String description;
    private List<String> suggestedPermissions;
    private boolean requiresApproval;
    private Integer maxAssignments;
}