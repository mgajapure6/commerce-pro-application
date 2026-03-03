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
 * Product Variant Entity - Maps to Angular ProductVariant interface
 */
@Entity
@Table(name = "product_variants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private String id;

    @NotBlank(message = "Variant name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "variant_options", joinColumns = @JoinColumn(name = "variant_id"))
    @Column(name = "option_value")
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
