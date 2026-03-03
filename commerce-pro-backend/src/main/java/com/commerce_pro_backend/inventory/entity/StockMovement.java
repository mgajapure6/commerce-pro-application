package com.commerce_pro_backend.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import com.commerce_pro_backend.catalog.product.entity.Product;

import java.time.LocalDateTime;

/**
 * StockMovement Entity - Tracks all inventory changes
 */
@Entity
@Table(name = "stock_movements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity;

    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;

    private String reason;

    @Column(length = 2000)
    private String notes;

    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private ReferenceType referenceType;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum MovementType {
        IN,          // Stock added
        OUT,         // Stock removed
        ADJUSTMENT,  // Manual adjustment
        TRANSFER_IN, // Transfer received
        TRANSFER_OUT,// Transfer sent
        RETURN,      // Customer return
        DAMAGED      // Damaged/lost
    }

    public enum ReferenceType {
        PURCHASE_ORDER,
        SALES_ORDER,
        TRANSFER,
        ADJUSTMENT,
        RETURN,
        COUNT
    }
}
