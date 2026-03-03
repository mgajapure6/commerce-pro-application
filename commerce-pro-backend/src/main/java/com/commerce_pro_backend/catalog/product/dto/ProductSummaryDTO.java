package com.commerce_pro_backend.catalog.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Product Summary DTO - matches Angular ProductSummary interface
 * Used for list views with minimal data
 */
@Data
@Builder
public class ProductSummaryDTO {

    private String id;
    private String name;
    private String sku;
    private String category;
    private String brand;
    private BigDecimal price;
    private Integer stock;
    private String image;
    private String status;
    private Boolean hasOrders;
}
