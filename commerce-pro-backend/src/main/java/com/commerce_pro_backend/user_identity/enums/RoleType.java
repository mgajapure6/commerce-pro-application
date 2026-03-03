// RoleType.java
package com.commerce_pro_backend.user_identity.enums;

public enum RoleType {
    SYSTEM,         // Core system roles (SUPER_ADMIN)
    GLOBAL,         // Cross-tenant roles
    TENANT,         // Tenant-scoped roles
    RESOURCE,       // Resource-specific roles
    DYNAMIC         // Auto-generated/conditional roles
}