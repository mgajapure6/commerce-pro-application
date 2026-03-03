package com.commerce_pro_backend.inventory.service;

import com.commerce_pro_backend.inventory.dto.LowStockAlertDTO;
import com.commerce_pro_backend.inventory.entity.Inventory;
import com.commerce_pro_backend.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Low Stock Alert Service
 */
@Service
@RequiredArgsConstructor
public class LowStockService {

    private final InventoryRepository inventoryRepository;

    /**
     * Get all low stock alerts
     */
    public List<LowStockAlertDTO> getLowStockAlerts() {
        List<Inventory> lowStockItems = inventoryRepository.findLowStock();
        List<LowStockAlertDTO> alerts = new ArrayList<>();
        
        for (Inventory item : lowStockItems) {
            alerts.add(convertToAlert(item));
        }
        
        return alerts;
    }

    /**
     * Get critical alerts (out of stock)
     */
    public List<LowStockAlertDTO> getCriticalAlerts() {
        List<Inventory> outOfStock = inventoryRepository.findOutOfStock();
        List<LowStockAlertDTO> alerts = new ArrayList<>();
        
        for (Inventory item : outOfStock) {
            alerts.add(convertToAlert(item));
        }
        
        return alerts;
    }

    /**
     * Get alerts by warehouse
     */
    public List<LowStockAlertDTO> getAlertsByWarehouse(String warehouseId) {
        List<Inventory> items = inventoryRepository.findByWarehouseId(warehouseId);
        List<LowStockAlertDTO> alerts = new ArrayList<>();
        
        for (Inventory item : items) {
            if (item.getStatus() == Inventory.StockStatus.LOW_STOCK || 
                item.getStatus() == Inventory.StockStatus.OUT_OF_STOCK) {
                alerts.add(convertToAlert(item));
            }
        }
        
        return alerts;
    }

    /**
     * Convert inventory item to alert DTO
     */
    private LowStockAlertDTO convertToAlert(Inventory item) {
        String status = determineAlertStatus(item);
        Integer daysUntilStockout = calculateDaysUntilStockout(item);
        
        return LowStockAlertDTO.builder()
                .id(UUID.randomUUID().toString())
                .inventoryItemId(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .productImage(item.getProduct().getImageUrl())
                .category(item.getProduct().getCategory())
                .warehouseId(item.getWarehouse().getId())
                .warehouseName(item.getWarehouse().getName())
                .currentStock(item.getQuantity())
                .availableStock(item.getAvailable())
                .lowStockThreshold(item.getLowStockThreshold())
                .reorderPoint(item.getReorderPoint())
                .reorderQuantity(item.getReorderQuantity())
                .status(status)
                .daysUntilStockout(daysUntilStockout)
                .avgDailyUsage(calculateAvgDailyUsage(item))
                .lastRestocked(item.getLastRestocked())
                .suggestedOrderDate(calculateSuggestedOrderDate(daysUntilStockout))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .acknowledged(false)
                .resolved(false)
                .build();
    }

    /**
     * Determine alert status based on stock levels
     */
    private String determineAlertStatus(Inventory item) {
        if (item.getStatus() == Inventory.StockStatus.OUT_OF_STOCK) {
            return "CRITICAL";
        } else if (item.getQuantity() <= (item.getReorderPoint() != null ? item.getReorderPoint() : item.getLowStockThreshold())) {
            return "REORDER";
        } else {
            return "LOW";
        }
    }

    /**
     * Calculate days until stockout based on average usage
     * This is a simplified calculation - in production would use historical data
     */
    private Integer calculateDaysUntilStockout(Inventory item) {
        BigDecimal avgUsage = calculateAvgDailyUsage(item);
        if (avgUsage.compareTo(BigDecimal.ZERO) == 0) {
            return null; // Unknown
        }
        
        BigDecimal days = BigDecimal.valueOf(item.getAvailable())
                .divide(avgUsage, 0, RoundingMode.HALF_UP);
        return days.intValue();
    }

    /**
     * Calculate average daily usage
     * In production, this would be calculated from historical sales data
     */
    private BigDecimal calculateAvgDailyUsage(Inventory item) {
        // Simplified: assume 5% of stock is used daily on average
        // In production, this would analyze historical movement data
        return BigDecimal.valueOf(item.getLowStockThreshold())
                .multiply(BigDecimal.valueOf(0.1))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate suggested order date
     */
    private LocalDateTime calculateSuggestedOrderDate(Integer daysUntilStockout) {
        if (daysUntilStockout == null || daysUntilStockout > 14) {
            return LocalDateTime.now().plusDays(7);
        } else if (daysUntilStockout > 7) {
            return LocalDateTime.now().plusDays(3);
        } else {
            return LocalDateTime.now().plusDays(1);
        }
    }
}
