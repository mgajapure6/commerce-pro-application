package com.commerce_pro_backend.catalog.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Product Attribute Entity - Dynamic attributes like Color, Size, Material
 * Used for creating product variants
 */
@Entity
@Table(name = "product_attributes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttribute {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private String id;

    @NotBlank(message = "Attribute name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    @Column(nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_attribute_values", joinColumns = @JoinColumn(name = "attribute_id"))
    @Column(name = "attr_value")
    @Builder.Default
    private List<String> values = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}
