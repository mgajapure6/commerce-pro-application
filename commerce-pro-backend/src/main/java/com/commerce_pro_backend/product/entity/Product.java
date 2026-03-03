package com.commerce_pro_backend.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Product Entity - Maps to Angular Product interface
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_status", columnList = "status"),
        @Index(name = "idx_product_category", columnList = "category"),
        @Index(name = "idx_product_brand", columnList = "brand"),
        @Index(name = "idx_product_sku", columnList = "sku", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private String id;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must be less than 100 characters")
    @Column(nullable = false, unique = true)
    private String sku;

    @Column(name = "description", length = 5000)
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must be less than 100 characters")
    @Column(nullable = false)
    private String category;

    @Size(max = 36, message = "Category ID must be less than 36 characters")
    @Column(name = "category_id")
    private String categoryId;

    @NotBlank(message = "Brand is required")
    @Size(max = 100, message = "Brand must be less than 100 characters")
    @Column(nullable = false)
    private String brand;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be 0 or greater")
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Compare price must be 0 or greater")
    @Column(name = "compare_at_price", precision = 19, scale = 4)
    private BigDecimal compareAtPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost must be 0 or greater")
    @Column(precision = 19, scale = 4)
    private BigDecimal cost;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stock;

    @NotNull(message = "Low stock threshold is required")
    @Min(value = 0, message = "Low stock threshold cannot be negative")
    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold;

    @NotBlank(message = "Stock status is required")
    @Column(name = "stock_status", nullable = false, length = 20)
    private String stockStatus;

    @NotBlank(message = "Status is required")
    @Column(nullable = false, length = 20)
    private String status;

    @NotBlank(message = "Visibility is required")
    @Column(nullable = false, length = 20)
    private String visibility;

    // Using TEXT type to support base64 data URLs (can be very long)
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_gallery", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> gallery = new ArrayList<>();

    @Column(name = "featured_image", columnDefinition = "TEXT")
    private String featuredImage;

    @DecimalMin(value = "0.0", inclusive = true, message = "Weight must be 0 or greater")
    private BigDecimal weight;

    @Embedded
    private Dimensions dimensions;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean featured = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean trackInventory = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean allowBackorders = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean hasOrders = false;

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "5.0", inclusive = true)
    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Min(0)
    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Min(0)
    @Column(name = "sales_count")
    @Builder.Default
    private Integer salesCount = 0;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal revenue = BigDecimal.ZERO;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ProductAttribute> attributes = new ArrayList<>();

    @Size(max = 100, message = "Vendor must be less than 100 characters")
    private String vendor;

    @Column(name = "product_type", length = 20)
    private String productType;

    @Size(max = 100, message = "Barcode must be less than 100 characters")
    private String barcode;

    @Size(max = 255, message = "URL handle must be less than 255 characters")
    @Column(name = "url_handle")
    private String urlHandle;

    @Size(max = 255, message = "SEO title must be less than 255 characters")
    @Column(name = "seo_title")
    private String seoTitle;

    @Size(max = 500, message = "SEO description must be less than 500 characters")
    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Size(max = 255, message = "Image alt text must be less than 255 characters")
    @Column(name = "image_alt")
    private String imageAlt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Update stock status based on current stock and threshold
     */
    public void updateStockStatus() {
        if (stock <= 0) {
            this.stockStatus = "out_of_stock";
        } else if (stock <= lowStockThreshold) {
            this.stockStatus = "low_stock";
        } else {
            this.stockStatus = "in_stock";
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersistUpdate() {
        updateStockStatus();
    }
}
