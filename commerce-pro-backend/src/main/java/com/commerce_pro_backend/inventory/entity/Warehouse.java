package com.commerce_pro_backend.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Warehouse Entity - Stores warehouse information
 */
@Entity
@Table(name = "warehouses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private String id;

    @NotBlank(message = "Warehouse name is required")
    @Column(nullable = false)
    private String name;

    private String code;

    @Column(length = 1000)
    private String address;

    private String city;
    private String state;
    private String country;
    private String postalCode;

    private String managerName;
    private String managerEmail;
    private String managerPhone;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isDefault = false;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Inventory> inventories = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
