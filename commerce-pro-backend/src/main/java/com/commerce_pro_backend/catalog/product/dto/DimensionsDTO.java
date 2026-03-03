package com.commerce_pro_backend.catalog.product.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dimensions DTO - matches Angular ProductDimensions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionsDTO {

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal length;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal width;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal height;
}
