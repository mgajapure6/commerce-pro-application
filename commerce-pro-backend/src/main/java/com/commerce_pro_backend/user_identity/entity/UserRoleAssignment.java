package com.commerce_pro_backend.user_identity.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.commerce_pro_backend.user_identity.enums.AssignmentStatus;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_role_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRoleAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssignmentStatus status;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "assigned_by", nullable = false)
    private String assignedBy;  // User ID of admin who assigned

    @CreationTimestamp
    @Column(name = "assigned_at", updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "revoked_by")
    private String revokedBy;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;

    @ElementCollection
    @CollectionTable(
        name = "role_assignment_constraints",
        joinColumns = @JoinColumn(name = "assignment_id")
    )
    @Column(name = "constraint_value")
    @Builder.Default
    private Set<String> constraints = new HashSet<>();  // IP restrictions, time windows, etc.

    @Column(name = "scope_context", length = 1000)
    private String scopeContext;  // JSON: {"vendorId": "123", "storeId": "456"}

    // Business methods
    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        if (status != AssignmentStatus.ACTIVE) return false;
        if (validFrom != null && validFrom.isAfter(now)) return false;
        if (validUntil != null && !validUntil.isAfter(now)) return false;
        return true;
    }
}