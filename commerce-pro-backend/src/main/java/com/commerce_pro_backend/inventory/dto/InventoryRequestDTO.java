package com.commerce_pro_backend.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Inventory Request DTO - For creating/updating inventory records
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequestDTO {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Min(value = 0, message = "Reserved quantity cannot be negative")
    private Integer reserved;

    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold;

    private Integer reorderPoint;
    private Integer reorderQuantity;
    private Integer maxStockLevel;
    private Integer safetyStock;

    private BigDecimal unitCost;

    private String binLocation;
    private String aisle;
    private String zone;

    private Boolean trackInventory;
}
