package com.commerce_pro_backend.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Product Filter DTO - matches Angular ProductFilterState interface
 * Used for filtering products in list queries
 */
@Data
@Builder
public class ProductFilterDTO {

    private String searchQuery;
    private String status;
    private String category;
    private String stockStatus;
    private String brand;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minRating;
    private Boolean featured;

    // Sorting
    private String sortBy;
    private String sortDirection; // asc, desc
}
