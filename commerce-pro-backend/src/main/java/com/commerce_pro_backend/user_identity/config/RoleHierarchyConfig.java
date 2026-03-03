package com.commerce_pro_backend.user_identity.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "commercepro.roles")
public class RoleHierarchyConfig {

    // Pre-defined role templates
    private Map<String, RoleTemplate> templates = new HashMap<>();
    
    // Hierarchy rules
    private HierarchyRules hierarchyRules = new HierarchyRules();

    @Data
    public static class RoleTemplate {
        private String name;
        private String description;
        private List<String> permissions = new ArrayList<>();
        private List<String> parentRoles = new ArrayList<>();
        private boolean requiresApproval = false;
        private Integer maxAssignments;  // Null = unlimited
        private Map<String, Object> defaultConstraints = new HashMap<>();
    }

    @Data
    public static class HierarchyRules {
        private int maxDepth = 5;  // Prevent infinite inheritance
        private boolean allowMultipleParents = true;  // DAG vs Tree
        private boolean enforceAcyclic = true;
        private List<String> immutableSystemRoles = List.of("SUPER_ADMIN");
    }
}