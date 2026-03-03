package com.commerce_pro_backend.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for stock update operations
 */
@Data
@Builder
public class StockUpdateDTO {

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason must be less than 255 characters")
    private String reason;

    // If true, adds to existing stock; if false, sets absolute value
    private Boolean adjust;
}
