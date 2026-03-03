package com.commerce_pro_backend.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * StockMovement DTO - Tracks inventory changes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementDTO {
    private String id;
    private String inventoryId;
    private String productId;
    private String warehouseId;
    private String productName;
    private String warehouseName;
    
    private String type;
    private Integer quantity;
    private Integer previousQuantity;
    private Integer newQuantity;
    
    private String reason;
    private String notes;
    private String reference;
    private String referenceType;
    
    private String createdBy;
    private LocalDateTime createdAt;
}
