package com.commerce_pro_backend.catalog.brand.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "brands",
    indexes = {
        @Index(name = "idx_brand_slug", columnList = "slug", unique = true),
        @Index(name = "idx_brand_active", columnList = "is_active"),
        @Index(name = "idx_brand_featured", columnList = "is_featured"),
        @Index(name = "idx_brand_sort", columnList = "sort_order")
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Brand {

    @Id
    @Column(length = 36)
    private String id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150, unique = true)
    private String slug;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @Size(max = 500)
    @Column(name = "logo_url", length = 500)
    private String logo;

    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$", message = "Invalid website URL")
    @Size(max = 500)
    @Column(length = 500)
    private String website;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "product_count", nullable = false)
    @Builder.Default
    private Integer productCount = 0;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    // Business methods

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void feature() {
        this.isFeatured = true;
    }

    public void unfeature() {
        this.isFeatured = false;
    }

    public void incrementProductCount() {
        this.productCount++;
    }

    public void decrementProductCount() {
        if (this.productCount > 0) {
            this.productCount--;
        }
    }

    public void updateProductCount(int count) {
        this.productCount = Math.max(0, count);
    }

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (this.slug != null) {
            this.slug = this.slug.toLowerCase().trim();
        }
        if (this.website != null && !this.website.isEmpty()) {
            if (!this.website.startsWith("http://") && !this.website.startsWith("https://")) {
                this.website = "https://" + this.website;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Brand)) return false;
        Brand brand = (Brand) o;
        return id != null && Objects.equals(id, brand.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}