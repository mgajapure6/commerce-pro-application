package com.commerce_pro_backend.catalog.product.service;

import com.commerce_pro_backend.catalog.product.dto.*;
import com.commerce_pro_backend.catalog.product.entity.Product;
import com.commerce_pro_backend.catalog.product.mapper.ProductMapper;
import com.commerce_pro_backend.catalog.product.repository.ProductRepository;
import com.commerce_pro_backend.catalog.product.specification.ProductSpecification;
import com.commerce_pro_backend.common.exception.ApiException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Product Service - Business logic layer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    // ==================== CRUD OPERATIONS ====================

    /**
     * Get product by ID
     */
    public ProductResponseDTO getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product", id));
        return productMapper.toResponseDTO(product);
    }

    /**
     * Get product by SKU
     */
    public ProductResponseDTO getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ApiException("Product not found with SKU: " + sku, HttpStatus.NOT_FOUND));
        return productMapper.toResponseDTO(product);
    }

    /**
     * Create new product
     */
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        // Validate SKU uniqueness
        if (productRepository.existsBySku(requestDTO.getSku())) {
            throw ApiException.conflict("Product with SKU '" + requestDTO.getSku() + "' already exists");
        }

        Product product = productMapper.toEntity(requestDTO);
        
        // Update stock status based on stock and threshold
        product.updateStockStatus();
        
        Product savedProduct = productRepository.save(product);
        log.info("Created product: {} with SKU: {}", savedProduct.getId(), savedProduct.getSku());
        
        return productMapper.toResponseDTO(savedProduct);
    }

    /**
     * Update existing product
     */
    @Transactional
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO requestDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product", id));

        // Validate SKU uniqueness if changed
        if (!existingProduct.getSku().equals(requestDTO.getSku()) 
                && productRepository.existsBySku(requestDTO.getSku())) {
            throw ApiException.conflict("Product with SKU '" + requestDTO.getSku() + "' already exists");
        }

        productMapper.updateEntityFromDTO(existingProduct, requestDTO);
        existingProduct.updateStockStatus();
        
        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Updated product: {}", updatedProduct.getId());
        
        return productMapper.toResponseDTO(updatedProduct);
    }

    /**
     * Delete product
     */
    @Transactional
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product", id));

        // Check if product has orders
        if (Boolean.TRUE.equals(product.getHasOrders())) {
            throw ApiException.conflict("Cannot delete product with existing orders. Archive it instead.");
        }

        productRepository.delete(product);
        log.info("Deleted product: {}", id);
    }

    /**
     * Patch product (partial update)
     */
    @Transactional
    public ProductResponseDTO patchProduct(String id, Map<String, Object> updates) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product", id));

        // Apply updates based on field name
        updates.forEach((field, value) -> {
            switch (field) {
                case "name" -> product.setName((String) value);
                case "price" -> product.setPrice(new BigDecimal(value.toString()));
                case "stock" -> product.setStock((Integer) value);
                case "status" -> product.setStatus((String) value);
                case "visibility" -> product.setVisibility((String) value);
                case "featured" -> product.setFeatured((Boolean) value);
                // Add more fields as needed
            }
        });

        product.updateStockStatus();
        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponseDTO(updatedProduct);
    }

    // ==================== LIST & SEARCH ====================

    /**
     * Get paginated products with filtering and sorting
     */
    public Page<ProductSummaryDTO> getProducts(ProductFilterDTO filter, Pageable pageable) {
        // Apply sorting from filter if provided
        if (StringUtils.hasText(filter.getSortBy())) {
            Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortDirection()) 
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            pageable = PageRequest.of(
                    pageable.getPageNumber(), 
                    pageable.getPageSize(),
                    Sort.by(direction, filter.getSortBy())
            );
        }

        Specification<Product> spec = ProductSpecification.withFilter(filter);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        
        return productPage.map(productMapper::toSummaryDTO);
    }

    /**
     * Get all products (for exports, etc.)
     */
    public List<ProductSummaryDTO> getAllProducts() {
        return productMapper.toSummaryDTOList(productRepository.findAll());
    }

    /**
     * Search products
     */
    public Page<ProductSummaryDTO> searchProducts(String query, Pageable pageable) {
        Page<Product> productPage = productRepository.search(query, pageable);
        return productPage.map(productMapper::toSummaryDTO);
    }

    /**
     * Get products by category
     */
    public Page<ProductSummaryDTO> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable)
                .map(productMapper::toSummaryDTO);
    }

    /**
     * Get featured products
     */
    public List<ProductSummaryDTO> getFeaturedProducts(int limit) {
        return productRepository.findAll(
                        Specification.where(ProductSpecification.isFeatured(true))
                                .and(ProductSpecification.forStorefront()),
                        PageRequest.of(0, limit)
                ).getContent()
                .stream()
                .map(productMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ==================== STOCK MANAGEMENT ====================

    /**
     * Update product stock
     */
    @Transactional
    public ProductResponseDTO updateStock(String id, StockUpdateDTO stockUpdate) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product", id));

        int newStock;
        if (Boolean.TRUE.equals(stockUpdate.getAdjust())) {
            // Adjust existing stock
            newStock = product.getStock() + stockUpdate.getQuantity();
        } else {
            // Set absolute stock
            newStock = stockUpdate.getQuantity();
        }

        if (newStock < 0) {
            throw ApiException.badRequest("Stock cannot be negative");
        }

        product.setStock(newStock);
        product.updateStockStatus();
        
        Product updatedProduct = productRepository.save(product);
        log.info("Updated stock for product {}: {} (reason: {})", 
                id, newStock, stockUpdate.getReason());
        
        return productMapper.toResponseDTO(updatedProduct);
    }

    /**
     * Get low stock products
     */
    public List<ProductSummaryDTO> getLowStockProducts() {
        return productRepository.findAll(ProductSpecification.isLowStock())
                .stream()
                .map(productMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get out of stock products
     */
    public List<ProductSummaryDTO> getOutOfStockProducts() {
        return productRepository.findAll(ProductSpecification.isOutOfStock())
                .stream()
                .map(productMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ==================== DASHBOARD & STATS ====================

    /**
     * Get top selling products
     */
    public List<ProductDashboardDTO> getTopSellingProducts(int limit) {
        return productRepository.findTopSelling(PageRequest.of(0, limit))
                .stream()
                .map(productMapper::toDashboardDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get top revenue products
     */
    public List<ProductDashboardDTO> getTopRevenueProducts(int limit) {
        return productRepository.findTopRevenue(PageRequest.of(0, limit))
                .stream()
                .map(productMapper::toDashboardDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get product statistics
     */
    public ProductStatsDTO getProductStats() {
        long total = productRepository.count();
        long active = productRepository.countByStatus("active");
        long drafts = productRepository.countByStatus("draft");
        long lowStock = productRepository.count(ProductSpecification.isLowStock());
        long outOfStock = productRepository.count(ProductSpecification.isOutOfStock());
        BigDecimal revenue = productRepository.sumRevenue();

        // Get status counts
        Map<String, Long> statusCounts = new HashMap<>();
        List<Object[]> groupedCounts = productRepository.countByStatusGrouped();
        for (Object[] row : groupedCounts) {
            statusCounts.put((String) row[0], (Long) row[1]);
        }

        return ProductStatsDTO.builder()
                .total(total)
                .active(active)
                .lowStock(lowStock)
                .outOfStock(outOfStock)
                .drafts(drafts)
                .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                .statusCounts(statusCounts)
                .build();
    }

    // ==================== REFERENCE DATA ====================

    /**
     * Get all unique brands
     */
    public List<String> getAllBrands() {
        return productRepository.findAllBrands();
    }

    /**
     * Get all unique tags
     */
    public List<String> getAllTags() {
        return productRepository.findAllTags();
    }

    /**
     * Check if SKU exists
     */
    public boolean skuExists(String sku) {
        return productRepository.existsBySku(sku);
    }

    /**
     * Check if SKU exists for another product
     */
    public boolean skuExistsForOtherProduct(String sku, String excludeId) {
        return productRepository.existsBySkuAndIdNot(sku, excludeId);
    }
}
