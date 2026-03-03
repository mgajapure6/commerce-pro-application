package com.commerce_pro_backend.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Product Variant DTO - matches Angular ProductVariant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {

    private String id;

    @NotBlank(message = "Variant name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    private List<String> options;
}
