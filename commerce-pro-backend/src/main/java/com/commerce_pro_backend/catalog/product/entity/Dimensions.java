package com.commerce_pro_backend.catalog.product.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Embeddable dimensions for Product
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dimensions {

    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
}
