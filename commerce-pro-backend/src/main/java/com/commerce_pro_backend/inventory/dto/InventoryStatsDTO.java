package com.commerce_pro_backend.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Inventory Statistics DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStatsDTO {

    private long totalItems;
    private long totalProducts;
    private long totalWarehouses;
    
    private long inStockCount;
    private long lowStockCount;
    private long outOfStockCount;
    private long overstockCount;
    private long notTrackedCount;
    
    private BigDecimal totalInventoryValue;
    private long totalUnits;
    private long totalReserved;
    private long totalAvailable;
    private long totalIncoming;
    
    private BigDecimal averageUnitCost;
    
    // Breakdown by status
    private Map<String, Long> statusBreakdown;
    
    // Breakdown by warehouse
    private Map<String, WarehouseStats> warehouseBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseStats {
        private String warehouseName;
        private long itemCount;
        private long totalUnits;
        private BigDecimal totalValue;
    }
}
