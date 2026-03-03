package com.commerce_pro_backend.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stock Transfer Request DTO - For transferring stock between warehouses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferRequestDTO {

    @NotBlank(message = "From warehouse ID is required")
    private String fromWarehouseId;

    @NotBlank(message = "To warehouse ID is required")
    private String toWarehouseId;

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String notes;
    private String reference;
}
