package com.commerce_pro_backend.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product Response DTO - matches Angular Product interface
 * Used for returning full product details
 */
@Data
@Builder
public class ProductResponseDTO {

    private String id;
    private String name;
    private String sku;
    private String description;
    private String shortDescription;
    private String category;
    private String categoryId;
    private String brand;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private BigDecimal comparePrice; // Alias for compareAtPrice
    private BigDecimal cost;
    private Integer stock;
    private Integer quantity; // Alias for stock
    private Integer lowStockThreshold;
    private String stockStatus;
    private String status;
    private String visibility;
    private String image;
    private List<String> gallery;
    private List<String> galleryImages; // Alias for gallery
    private String featuredImage;
    private BigDecimal weight;
    private DimensionsDTO dimensions;
    private List<String> tags;
    private Boolean featured;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer reviews; // Alias for reviewCount
    private Integer salesCount;
    private Integer sales; // Alias for salesCount
    private BigDecimal revenue;
    private List<ProductVariantDTO> variants;
    private List<ProductAttributeDTO> attributes;
    private Integer variantCount;
    private Boolean hasOrders;
    private Boolean trackInventory;
    private Boolean allowBackorders;
    private String vendor;
    private String productType;
    private String barcode;
    private String urlHandle;
    private String seoTitle;
    private String seoDescription;
    private String imageAlt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
