package com.commerce_pro_backend.product.mapper;

import com.commerce_pro_backend.product.dto.*;
import com.commerce_pro_backend.product.entity.Dimensions;
import com.commerce_pro_backend.product.entity.Product;
import com.commerce_pro_backend.product.entity.ProductAttribute;
import com.commerce_pro_backend.product.entity.ProductVariant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Product entities and DTOs
 */
@Component
public class ProductMapper {

    // ==================== TO ENTITY ====================

    public Product toEntity(ProductRequestDTO dto) {
        if (dto == null) return null;

        Product product = Product.builder()
                .name(dto.getName())
                .sku(dto.getSku())
                .description(dto.getDescription())
                .shortDescription(dto.getShortDescription())
                .category(dto.getCategory())
                .categoryId(dto.getCategoryId())
                .brand(dto.getBrand())
                .price(dto.getPrice())
                .compareAtPrice(dto.getCompareAtPrice())
                .cost(dto.getCost())
                .stock(dto.getStock())
                .lowStockThreshold(dto.getLowStockThreshold())
                .status(dto.getStatus())
                .visibility(dto.getVisibility())
                .imageUrl(dto.getImage())
                .gallery(dto.getGallery() != null ? dto.getGallery() : List.of())
                .featuredImage(dto.getFeaturedImage())
                .weight(dto.getWeight())
                .dimensions(toDimensionsEntity(dto.getDimensions()))
                .tags(dto.getTags() != null ? dto.getTags() : List.of())
                .featured(dto.getFeatured())
                .trackInventory(dto.getTrackInventory())
                .allowBackorders(dto.getAllowBackorders())
                .vendor(dto.getVendor())
                .productType(dto.getProductType())
                .barcode(dto.getBarcode())
                .urlHandle(dto.getUrlHandle())
                .seoTitle(dto.getSeoTitle())
                .seoDescription(dto.getSeoDescription())
                .imageAlt(dto.getImageAlt())
                .build();

        // Map variants
        if (dto.getVariants() != null) {
            List<ProductVariant> variants = dto.getVariants().stream()
                    .map(v -> toVariantEntity(v, product))
                    .collect(Collectors.toList());
            product.setVariants(variants);
        }

        // Map attributes
        if (dto.getAttributes() != null) {
            List<ProductAttribute> attributes = dto.getAttributes().stream()
                    .map(a -> toAttributeEntity(a, product))
                    .collect(Collectors.toList());
            product.setAttributes(attributes);
        }

        return product;
    }

    public void updateEntityFromDTO(Product product, ProductRequestDTO dto) {
        if (dto == null) return;

        product.setName(dto.getName());
        product.setSku(dto.getSku());
        product.setDescription(dto.getDescription());
        product.setShortDescription(dto.getShortDescription());
        product.setCategory(dto.getCategory());
        product.setCategoryId(dto.getCategoryId());
        product.setBrand(dto.getBrand());
        product.setPrice(dto.getPrice());
        product.setCompareAtPrice(dto.getCompareAtPrice());
        product.setCost(dto.getCost());
        product.setStock(dto.getStock());
        product.setLowStockThreshold(dto.getLowStockThreshold());
        product.setStatus(dto.getStatus());
        product.setVisibility(dto.getVisibility());
        product.setImageUrl(dto.getImage());
        product.setGallery(dto.getGallery() != null ? dto.getGallery() : List.of());
        product.setFeaturedImage(dto.getFeaturedImage());
        product.setWeight(dto.getWeight());
        product.setDimensions(toDimensionsEntity(dto.getDimensions()));
        product.setTags(dto.getTags() != null ? dto.getTags() : List.of());
        product.setFeatured(dto.getFeatured());
        product.setTrackInventory(dto.getTrackInventory());
        product.setAllowBackorders(dto.getAllowBackorders());
        product.setVendor(dto.getVendor());
        product.setProductType(dto.getProductType());
        product.setBarcode(dto.getBarcode());
        product.setUrlHandle(dto.getUrlHandle());
        product.setSeoTitle(dto.getSeoTitle());
        product.setSeoDescription(dto.getSeoDescription());
        product.setImageAlt(dto.getImageAlt());

        // Update variants - merge strategy: update existing, add new, remove deleted
        if (dto.getVariants() != null) {
            updateVariants(product, dto.getVariants());
        }

        // Update attributes - merge strategy
        if (dto.getAttributes() != null) {
            updateAttributes(product, dto.getAttributes());
        }
    }

    /**
     * Update product variants with merge strategy:
     * - Variants with ID (existing): update in place
     * - Variants without ID (new): create new
     * - Variants not in DTO: remove from product
     */
    private void updateVariants(Product product, List<ProductVariantDTO> variantDTOs) {
        List<ProductVariant> existingVariants = product.getVariants();
        List<ProductVariant> updatedVariants = new ArrayList<>();
        
        for (ProductVariantDTO dto : variantDTOs) {
            if (dto.getId() != null && !dto.getId().isEmpty()) {
                // Try to find existing variant by ID
                ProductVariant existingVariant = existingVariants.stream()
                        .filter(v -> dto.getId().equals(v.getId()))
                        .findFirst()
                        .orElse(null);
                
                if (existingVariant != null) {
                    // Update existing variant
                    existingVariant.setName(dto.getName());
                    existingVariant.setOptions(dto.getOptions() != null ? dto.getOptions() : new ArrayList<>());
                    updatedVariants.add(existingVariant);
                } else {
                    // ID provided but not found - create new (shouldn't happen normally)
                    updatedVariants.add(createNewVariant(dto, product));
                }
            } else {
                // No ID - create new variant
                updatedVariants.add(createNewVariant(dto, product));
            }
        }
        
        // Clear and add all (orphanRemoval will delete removed variants)
        existingVariants.clear();
        existingVariants.addAll(updatedVariants);
    }

    private ProductVariant createNewVariant(ProductVariantDTO dto, Product product) {
        ProductVariant variant = new ProductVariant();
        variant.setName(dto.getName());
        variant.setOptions(dto.getOptions() != null ? dto.getOptions() : new ArrayList<>());
        variant.setProduct(product);
        // ID will be auto-generated by @UuidGenerator
        return variant;
    }

    // ==================== ATTRIBUTE MAPPING ====================

    private ProductAttribute toAttributeEntity(ProductAttributeDTO dto, Product product) {
        return createNewAttribute(dto, product);
    }

    private ProductAttribute createNewAttribute(ProductAttributeDTO dto, Product product) {
        ProductAttribute attribute = new ProductAttribute();
        attribute.setName(dto.getName());
        attribute.setValues(dto.getValues() != null ? dto.getValues() : new ArrayList<>());
        attribute.setDisplayOrder(dto.getDisplayOrder());
        attribute.setProduct(product);
        // ID will be auto-generated by @UuidGenerator
        return attribute;
    }

    /**
     * Update product attributes with merge strategy
     */
    private void updateAttributes(Product product, List<ProductAttributeDTO> attributeDTOs) {
        List<ProductAttribute> existingAttributes = product.getAttributes();
        List<ProductAttribute> updatedAttributes = new ArrayList<>();
        
        for (ProductAttributeDTO dto : attributeDTOs) {
            if (dto.getId() != null && !dto.getId().isEmpty()) {
                // Try to find existing attribute by ID
                ProductAttribute existingAttribute = existingAttributes.stream()
                        .filter(a -> dto.getId().equals(a.getId()))
                        .findFirst()
                        .orElse(null);
                
                if (existingAttribute != null) {
                    // Update existing attribute
                    existingAttribute.setName(dto.getName());
                    existingAttribute.setValues(dto.getValues() != null ? dto.getValues() : new ArrayList<>());
                    existingAttribute.setDisplayOrder(dto.getDisplayOrder());
                    updatedAttributes.add(existingAttribute);
                } else {
                    // ID provided but not found - create new
                    updatedAttributes.add(createNewAttribute(dto, product));
                }
            } else {
                // No ID - create new attribute
                updatedAttributes.add(createNewAttribute(dto, product));
            }
        }
        
        // Clear and add all (orphanRemoval will delete removed attributes)
        existingAttributes.clear();
        existingAttributes.addAll(updatedAttributes);
    }

    private ProductAttributeDTO toAttributeDTO(ProductAttribute attribute) {
        if (attribute == null) return null;
        return ProductAttributeDTO.builder()
                .id(attribute.getId())
                .name(attribute.getName())
                .values(attribute.getValues())
                .displayOrder(attribute.getDisplayOrder())
                .build();
    }

    private Dimensions toDimensionsEntity(DimensionsDTO dto) {
        if (dto == null) return null;
        return Dimensions.builder()
                .length(dto.getLength())
                .width(dto.getWidth())
                .height(dto.getHeight())
                .build();
    }

    private ProductVariant toVariantEntity(ProductVariantDTO dto, Product product) {
        // Delegate to createNewVariant for consistency
        return createNewVariant(dto, product);
    }

    // ==================== TO DTO ====================

    public ProductResponseDTO toResponseDTO(Product product) {
        if (product == null) return null;

        List<ProductVariantDTO> variantDTOs = product.getVariants() != null
                ? product.getVariants().stream().map(this::toVariantDTO).collect(Collectors.toList())
                : List.of();

        List<ProductAttributeDTO> attributeDTOs = product.getAttributes() != null
                ? product.getAttributes().stream().map(this::toAttributeDTO).collect(Collectors.toList())
                : List.of();

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .category(product.getCategory())
                .categoryId(product.getCategoryId())
                .brand(product.getBrand())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .comparePrice(product.getCompareAtPrice()) // Alias
                .cost(product.getCost())
                .stock(product.getStock())
                .quantity(product.getStock()) // Alias
                .lowStockThreshold(product.getLowStockThreshold())
                .stockStatus(product.getStockStatus())
                .status(product.getStatus())
                .visibility(product.getVisibility())
                .image(product.getImageUrl())
                .gallery(product.getGallery())
                .galleryImages(product.getGallery()) // Alias
                .featuredImage(product.getFeaturedImage())
                .weight(product.getWeight())
                .dimensions(toDimensionsDTO(product.getDimensions()))
                .tags(product.getTags())
                .featured(product.getFeatured())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .reviews(product.getReviewCount()) // Alias
                .salesCount(product.getSalesCount())
                .sales(product.getSalesCount()) // Alias
                .revenue(product.getRevenue())
                .variants(variantDTOs)
                .variantCount(variantDTOs.size())
                .attributes(attributeDTOs)
                .hasOrders(product.getHasOrders())
                .trackInventory(product.getTrackInventory())
                .allowBackorders(product.getAllowBackorders())
                .vendor(product.getVendor())
                .productType(product.getProductType())
                .barcode(product.getBarcode())
                .urlHandle(product.getUrlHandle())
                .seoTitle(product.getSeoTitle())
                .seoDescription(product.getSeoDescription())
                .imageAlt(product.getImageAlt())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public ProductSummaryDTO toSummaryDTO(Product product) {
        if (product == null) return null;

        return ProductSummaryDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .category(product.getCategory())
                .brand(product.getBrand())
                .price(product.getPrice())
                .stock(product.getStock())
                .image(product.getImageUrl())
                .status(product.getStatus())
                .hasOrders(product.getHasOrders())
                .build();
    }

    public ProductDashboardDTO toDashboardDTO(Product product) {
        if (product == null) return null;

        return ProductDashboardDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .sold(product.getSalesCount())
                .revenue(product.getRevenue())
                .stock(product.getStock())
                .stockStatus(product.getStockStatus())
                .image(product.getImageUrl())
                .build();
    }

    private ProductVariantDTO toVariantDTO(ProductVariant variant) {
        if (variant == null) return null;
        return ProductVariantDTO.builder()
                .id(variant.getId())
                .name(variant.getName())
                .options(variant.getOptions())
                .build();
    }

    private DimensionsDTO toDimensionsDTO(Dimensions dimensions) {
        if (dimensions == null) return null;
        return DimensionsDTO.builder()
                .length(dimensions.getLength())
                .width(dimensions.getWidth())
                .height(dimensions.getHeight())
                .build();
    }

    // ==================== LIST MAPPING ====================

    public List<ProductResponseDTO> toResponseDTOList(List<Product> products) {
        return products.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductSummaryDTO> toSummaryDTOList(List<Product> products) {
        return products.stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDashboardDTO> toDashboardDTOList(List<Product> products) {
        return products.stream()
                .map(this::toDashboardDTO)
                .collect(Collectors.toList());
    }
}
