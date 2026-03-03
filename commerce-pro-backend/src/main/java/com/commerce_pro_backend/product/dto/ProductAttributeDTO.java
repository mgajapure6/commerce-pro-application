package com.commerce_pro_backend.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for ProductAttribute - represents a dynamic attribute like Color, Size, Material
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeDTO {
    private String id;
    private String name;           // e.g., "Color", "Size"
    @Builder.Default
    private List<String> values = new ArrayList<>();  // e.g., ["Red", "Blue", "Green"]
    private Integer displayOrder;  // For ordering attributes
}
