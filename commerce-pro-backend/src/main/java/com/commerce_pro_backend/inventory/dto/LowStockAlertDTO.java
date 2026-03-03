package com.commerce_pro_backend.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Low Stock Alert DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlertDTO {
    
    private String id;
    private String inventoryItemId;
    private String productId;
    private String productName;
    private String productSku;
    private String productImage;
    private String category;
    
    private String warehouseId;
    private String warehouseName;
    
    private Integer currentStock;
    private Integer availableStock;
    private Integer lowStockThreshold;
    private Integer reorderPoint;
    private Integer reorderQuantity;
    
    private String status; // CRITICAL, LOW, REORDER
    private Integer daysUntilStockout;
    private BigDecimal avgDailyUsage;
    
    private LocalDateTime lastRestocked;
    private LocalDateTime suggestedOrderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // For alerts
    private Boolean acknowledged;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private Boolean resolved;
    private LocalDateTime resolvedAt;
}
