package com.commerce_pro_backend.user_identity.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.commerce_pro_backend.user_identity.enums.PermissionCategory;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Permission {

    @Id
    @Column(name = "code", length = 100)
    private String code;  // identity:user:create, identity:role:delete, etc.

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private PermissionCategory category;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;  // Cannot be deleted if true

    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = false;  // Super admin can require approval for sensitive perms

    @Column(name = "risk_level", nullable = false)
    @Builder.Default
    private Integer riskLevel = 1;  // 1-5, for audit and monitoring

    @ElementCollection
    @CollectionTable(
        name = "permission_scopes",
        joinColumns = @JoinColumn(name = "permission_code")
    )
    @Column(name = "scope")
    @Builder.Default
    private Set<String> applicableScopes = new HashSet<>();  // any, own, department, etc.

    @ManyToMany(mappedBy = "permissions")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    // Business methods
    public boolean isManageableBySuperAdmin() {
        return category == PermissionCategory.IDENTITY_MANAGEMENT || 
               category == PermissionCategory.SYSTEM_CONFIGURATION;
    }
}