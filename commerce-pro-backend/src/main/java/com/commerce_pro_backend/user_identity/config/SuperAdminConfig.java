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
@ConfigurationProperties(prefix = "commercepro.superadmin")
public class SuperAdminConfig {

    // Default credentials (change immediately in production)
    private DefaultCredentials defaultCredentials = new DefaultCredentials();
    
    // Security policies
    private SecurityPolicy securityPolicy = new SecurityPolicy();
    
    // Permissions that super admin automatically gets
    private List<String> autoPermissions = new ArrayList<>();
    
    // Configuration management
    private ConfigurationManagement configManagement = new ConfigurationManagement();
    
    // UI customization for super admin
    private UiCustomization uiCustomization = new UiCustomization();

    @Data
    public static class DefaultCredentials {
        private String username = "superadmin";
        private String email = "superadmin@commercepro.local";
        private String password = "${SUPERADMIN_PASSWORD:ChangeMe123!}";
        private boolean forcePasswordChangeOnFirstLogin = true;
        private boolean enabled = true;
    }

    @Data
    public static class SecurityPolicy {
        private boolean requireMfa = false;
        private int sessionTimeoutMinutes = 30;
        private int maxConcurrentSessions = 3;
        private List<String> allowedIpRanges = new ArrayList<>();  // Empty = any IP
        private boolean restrictToLocalhostInDev = true;
        private boolean auditAllActions = true;
        private boolean notifyOnSensitiveChanges = true;
        private List<String> sensitivePermissions = List.of(
            "identity:user:delete",
            "identity:role:delete",
            "identity:permission:system-modify"
        );
    }

    @Data
    public static class ConfigurationManagement {
        private boolean canModifySystemPermissions = true;
        private boolean canDeleteSystemRoles = false;  // Safety lock
        private boolean canModifyOwnRole = false;      // Prevent lockout
        private boolean canAssignSuperAdmin = true;    // Can create other super admins
        private int minSuperAdmins = 1;                // Prevent last super admin deletion
        private boolean allowRemoteConfigReload = true;
        private Map<String, Object> defaultSettings = new HashMap<>();
    }

    @Data
    public static class UiCustomization {
        private String theme = "dark";
        private boolean showSystemDebugInfo = true;
        private boolean enableRawQueryMode = false;  // Allow SQL-like queries in UI
        private List<String> dashboardWidgets = List.of(
            "user-stats", "security-alerts", "audit-summary", "system-health"
        );
        private Map<String, String> customLinks = new HashMap<>();
    }
}