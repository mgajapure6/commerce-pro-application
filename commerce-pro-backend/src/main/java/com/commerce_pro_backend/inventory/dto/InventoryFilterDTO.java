package com.commerce_pro_backend.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Inventory Filter DTO - For filtering inventory queries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryFilterDTO {

    private String searchQuery;
    private String warehouseId;
    private String status;
    private String category;
    private String productId;
    private Boolean trackInventory;
    
    private BigDecimal minValue;
    private BigDecimal maxValue;
    
    private Integer minQuantity;
    private Integer maxQuantity;
    
    private Boolean lowStockOnly;
    private Boolean outOfStockOnly;
    
    private String sortBy;
    private String sortDirection;
}
