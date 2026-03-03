package com.commerce_pro_backend.catalog.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Product Statistics DTO - matches Angular ProductStats interface
 */
@Data
@Builder
public class ProductStatsDTO {

    private long total;
    private long active;
    private long lowStock;
    private long outOfStock;
    private long drafts;
    private BigDecimal revenue;

    // Additional breakdown by status
    private Map<String, Long> statusCounts;
}
