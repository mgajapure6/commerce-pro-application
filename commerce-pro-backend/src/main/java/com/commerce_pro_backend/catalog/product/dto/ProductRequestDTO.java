package com.commerce_pro_backend.catalog.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Request DTO - for creating/updating products
 * Used for POST /products and PUT /products/{id}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must be less than 100 characters")
    private String sku;

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;

    @Size(max = 500, message = "Short description must be less than 500 characters")
    private String shortDescription;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must be less than 100 characters")
    private String category;

    @Size(max = 36, message = "Category ID must be less than 36 characters")
    private String categoryId;

    @NotBlank(message = "Brand is required")
    @Size(max = 100, message = "Brand must be less than 100 characters")
    private String brand;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be 0 or greater")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Compare price must be 0 or greater")
    private BigDecimal compareAtPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost must be 0 or greater")
    private BigDecimal cost;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotNull(message = "Low stock threshold is required")
    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "active|draft|archived|out_of_stock|discontinued", message = "Invalid status")
    private String status;

    @NotBlank(message = "Visibility is required")
    @Pattern(regexp = "visible|hidden", message = "Invalid visibility")
    private String visibility;

    // Note: Size limit removed to support base64 data URLs (can be very long)
    // In production, use actual file upload and store URLs only
    private String image;

    private List<String> gallery;

    private String featuredImage;

    @DecimalMin(value = "0.0", inclusive = true, message = "Weight must be 0 or greater")
    private BigDecimal weight;

    @Valid
    private DimensionsDTO dimensions;

    private List<@Size(max = 50) String> tags;

    @NotNull(message = "Featured flag is required")
    private Boolean featured;

    @Valid
    private List<ProductVariantDTO> variants;

    @Valid
    private List<ProductAttributeDTO> attributes;

    @NotNull(message = "Track inventory flag is required")
    private Boolean trackInventory;

    @NotNull(message = "Allow backorders flag is required")
    private Boolean allowBackorders;

    @Size(max = 100, message = "Vendor must be less than 100 characters")
    private String vendor;

    @Pattern(regexp = "Physical|Digital", message = "Product type must be Physical or Digital")
    private String productType;

    @Size(max = 100, message = "Barcode must be less than 100 characters")
    private String barcode;

    @Size(max = 255, message = "URL handle must be less than 255 characters")
    private String urlHandle;

    @Size(max = 255, message = "SEO title must be less than 255 characters")
    private String seoTitle;

    @Size(max = 1000, message = "SEO description must be less than 1000 characters")
    private String seoDescription;

    @Size(max = 255, message = "Image alt text must be less than 255 characters")
    private String imageAlt;
}
