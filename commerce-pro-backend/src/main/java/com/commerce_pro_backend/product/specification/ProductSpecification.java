package com.commerce_pro_backend.product.specification;

import com.commerce_pro_backend.product.dto.ProductFilterDTO;
import com.commerce_pro_backend.product.entity.Product;
import com.commerce_pro_backend.product.entity.ProductVariant;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic Product filtering
 * Supports all filter criteria from Angular ProductFilterState
 */
public class ProductSpecification {

    /**
     * Build specification from filter DTO
     */
    public static Specification<Product> withFilter(ProductFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search query (name, sku, description, brand)
            if (StringUtils.hasText(filter.getSearchQuery())) {
                String search = "%" + filter.getSearchQuery().toLowerCase() + "%";
                Predicate searchPredicate = cb.or(
                        cb.like(cb.lower(root.get("name")), search),
                        cb.like(cb.lower(root.get("sku")), search),
                        cb.like(cb.lower(root.get("description")), search),
                        cb.like(cb.lower(root.get("brand")), search)
                );
                predicates.add(searchPredicate);
            }

            // Status filter
            if (StringUtils.hasText(filter.getStatus())) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Category filter
            if (StringUtils.hasText(filter.getCategory())) {
                predicates.add(cb.equal(root.get("category"), filter.getCategory()));
            }

            // Stock status filter
            if (StringUtils.hasText(filter.getStockStatus())) {
                predicates.add(cb.equal(root.get("stockStatus"), filter.getStockStatus()));
            }

            // Brand filter
            if (StringUtils.hasText(filter.getBrand())) {
                predicates.add(cb.equal(root.get("brand"), filter.getBrand()));
            }

            // Price range filter
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            // Rating filter
            if (filter.getMinRating() != null && filter.getMinRating() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), filter.getMinRating()));
            }

            // Featured filter
            if (filter.getFeatured() != null) {
                predicates.add(cb.equal(root.get("featured"), filter.getFeatured()));
            }

            // Visibility - only visible products for public queries
            // This can be conditionally added based on context

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Search by name, SKU, or brand
     */
    public static Specification<Product> search(String query) {
        return (root, criteriaQuery, cb) -> {
            if (!StringUtils.hasText(query)) {
                return cb.conjunction();
            }
            String search = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), search),
                    cb.like(cb.lower(root.get("sku")), search),
                    cb.like(cb.lower(root.get("brand")), search)
            );
        };
    }

    /**
     * Filter by status
     */
    public static Specification<Product> hasStatus(String status) {
        return (root, query, cb) -> 
            StringUtils.hasText(status) ? cb.equal(root.get("status"), status) : cb.conjunction();
    }

    /**
     * Filter by category
     */
    public static Specification<Product> hasCategory(String category) {
        return (root, query, cb) -> 
            StringUtils.hasText(category) ? cb.equal(root.get("category"), category) : cb.conjunction();
    }

    /**
     * Filter by stock status
     */
    public static Specification<Product> hasStockStatus(String stockStatus) {
        return (root, query, cb) -> 
            StringUtils.hasText(stockStatus) ? cb.equal(root.get("stockStatus"), stockStatus) : cb.conjunction();
    }

    /**
     * Filter by brand
     */
    public static Specification<Product> hasBrand(String brand) {
        return (root, query, cb) -> 
            StringUtils.hasText(brand) ? cb.equal(root.get("brand"), brand) : cb.conjunction();
    }

    /**
     * Filter by visibility
     */
    public static Specification<Product> hasVisibility(String visibility) {
        return (root, query, cb) -> 
            StringUtils.hasText(visibility) ? cb.equal(root.get("visibility"), visibility) : cb.conjunction();
    }

    /**
     * Filter by featured
     */
    public static Specification<Product> isFeatured(boolean featured) {
        return (root, query, cb) -> cb.equal(root.get("featured"), featured);
    }

    /**
     * Filter by price range
     */
    public static Specification<Product> priceBetween(java.math.BigDecimal min, java.math.BigDecimal max) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (min != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), min));
            }
            if (max != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), max));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filter by rating
     */
    public static Specification<Product> hasMinRating(int minRating) {
        return (root, query, cb) -> 
            minRating > 0 ? cb.greaterThanOrEqualTo(root.get("rating"), minRating) : cb.conjunction();
    }

    /**
     * Filter by tag
     */
    public static Specification<Product> hasTag(String tag) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(tag)) {
                return cb.conjunction();
            }
            return cb.isMember(tag, root.get("tags"));
        };
    }

    /**
     * Filter by variant name (for products with specific variants)
     */
    public static Specification<Product> hasVariantName(String variantName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(variantName)) {
                return cb.conjunction();
            }
            Join<Product, ProductVariant> variants = root.join("variants");
            return cb.like(cb.lower(variants.get("name")), "%" + variantName.toLowerCase() + "%");
        };
    }

    /**
     * Combined specification for active and visible products (for storefront)
     */
    public static Specification<Product> forStorefront() {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), "active"),
                cb.equal(root.get("visibility"), "visible")
        );
    }

    /**
     * Low stock products
     */
    public static Specification<Product> isLowStock() {
        return (root, query, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get("stock"), root.get("lowStockThreshold")),
                cb.greaterThan(root.get("stock"), 0)
        );
    }

    /**
     * Out of stock products
     */
    public static Specification<Product> isOutOfStock() {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("stock"), 0);
    }
}
