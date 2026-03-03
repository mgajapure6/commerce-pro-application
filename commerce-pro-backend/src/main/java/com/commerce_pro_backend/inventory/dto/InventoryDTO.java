package com.commerce_pro_backend.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.commerce_pro_backend.catalog.product.dto.ProductSummaryDTO;

/**
 * Inventory DTO - Main inventory record response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private String id;
    private String productId;
    private String warehouseId;
    private ProductSummaryDTO product;
    private WarehouseDTO warehouse;
    
    private Integer quantity;
    private Integer reserved;
    private Integer available;
    private Integer incoming;
    
    private Integer lowStockThreshold;
    private Integer reorderPoint;
    private Integer reorderQuantity;
    private Integer maxStockLevel;
    private Integer safetyStock;
    
    private BigDecimal unitCost;
    private BigDecimal totalValue;
    
    private String binLocation;
    private String aisle;
    private String zone;
    
    private Boolean trackInventory;
    private String status;
    
    private LocalDateTime lastRestocked;
    private LocalDateTime lastCounted;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
