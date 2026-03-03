package com.commerce_pro_backend.inventory.entity;

import com.commerce_pro_backend.inventory.entity.StockMovement.MovementType;
import com.commerce_pro_backend.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory Entity - Main inventory record for a product in a warehouse
 */
@Entity
@Table(name = "inventory", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "warehouse_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reserved = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer available = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer incoming = 0;

    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Column(name = "reorder_point")
    private Integer reorderPoint;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    @Column(name = "safety_stock")
    private Integer safetyStock;

    @Column(name = "unit_cost")
    private BigDecimal unitCost;

    @Column(name = "total_value")
    private BigDecimal totalValue;

    private String binLocation;
    private String aisle;
    private String zone;

    @Column(nullable = false)
    @Builder.Default
    private Boolean trackInventory = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StockStatus status = StockStatus.IN_STOCK;

    @Column(name = "last_restocked")
    private LocalDateTime lastRestocked;

    @Column(name = "last_counted")
    private LocalDateTime lastCounted;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("createdAt DESC")
    private List<StockMovement> movements = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        updateAvailable();
        updateTotalValue();
        updateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateAvailable();
        updateTotalValue();
        updateStatus();
    }

    private void updateAvailable() {
        this.available = Math.max(0, this.quantity - this.reserved);
    }

    private void updateTotalValue() {
        if (this.unitCost != null && this.quantity != null) {
            this.totalValue = this.unitCost.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    public void updateStatus() {
        if (!trackInventory) {
            this.status = StockStatus.NOT_TRACKED;
        } else if (quantity <= 0) {
            this.status = StockStatus.OUT_OF_STOCK;
        } else if (quantity <= lowStockThreshold) {
            this.status = StockStatus.LOW_STOCK;
        } else if (maxStockLevel != null && quantity > maxStockLevel) {
            this.status = StockStatus.OVERSTOCK;
        } else {
            this.status = StockStatus.IN_STOCK;
        }
    }

    public void adjustStock(int newQuantity, String reason, String notes, String reference) {
        int difference = newQuantity - this.quantity;
        
        StockMovement movement = StockMovement.builder()
                .inventory(this)
                .product(this.product)
                .warehouse(this.warehouse)
                .type(difference >= 0 ? StockMovement.MovementType.IN : StockMovement.MovementType.ADJUSTMENT)
                .quantity(Math.abs(difference))
                .previousQuantity(this.quantity)
                .newQuantity(newQuantity)
                .reason(reason)
                .notes(notes)
                .reference(reference)
                .build();
        
        this.movements.add(movement);
        this.quantity = newQuantity;
        
        if (difference > 0) {
            this.lastRestocked = LocalDateTime.now();
        }
        
        updateAvailable();
        updateTotalValue();
        updateStatus();
    }

    public void reserve(int amount) {
        if (amount <= 0) return;
        this.reserved = Math.min(this.quantity, this.reserved + amount);
        updateAvailable();
    }

    public void releaseReservation(int amount) {
        if (amount <= 0) return;
        this.reserved = Math.max(0, this.reserved - amount);
        updateAvailable();
    }

    public enum StockStatus {
        IN_STOCK,
        LOW_STOCK,
        OUT_OF_STOCK,
        OVERSTOCK,
        NOT_TRACKED
    }
}
