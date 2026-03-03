package com.commerce_pro_backend.catalog.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Product Dashboard DTO - matches Angular ProductDashboardView interface
 * Used for dashboard/top products view
 */
@Data
@Builder
public class ProductDashboardDTO {

    private String id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer sold;
    private BigDecimal revenue;
    private Integer stock;
    private String stockStatus;
    private String icon;
    private String image;
}
