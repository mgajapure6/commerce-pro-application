package com.commerce_pro_backend.inventory.specification;

import com.commerce_pro_backend.inventory.dto.InventoryFilterDTO;
import com.commerce_pro_backend.inventory.entity.Inventory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Inventory Specification for dynamic filtering
 */
public class InventorySpecification {

    public static Specification<Inventory> withFilter(InventoryFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getWarehouseId() != null && !filter.getWarehouseId().isEmpty()) {
                predicates.add(cb.equal(root.get("warehouse").get("id"), filter.getWarehouseId()));
            }

            if (filter.getProductId() != null && !filter.getProductId().isEmpty()) {
                predicates.add(cb.equal(root.get("product").get("id"), filter.getProductId()));
            }

            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), Inventory.StockStatus.valueOf(filter.getStatus())));
            }

            if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
                predicates.add(cb.equal(root.get("product").get("category"), filter.getCategory()));
            }

            if (filter.getTrackInventory() != null) {
                predicates.add(cb.equal(root.get("trackInventory"), filter.getTrackInventory()));
            }

            if (filter.getLowStockOnly() != null && filter.getLowStockOnly()) {
                predicates.add(cb.equal(root.get("status"), Inventory.StockStatus.LOW_STOCK));
            }

            if (filter.getOutOfStockOnly() != null && filter.getOutOfStockOnly()) {
                predicates.add(cb.equal(root.get("status"), Inventory.StockStatus.OUT_OF_STOCK));
            }

            if (filter.getMinQuantity() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("quantity"), filter.getMinQuantity()));
            }

            if (filter.getMaxQuantity() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("quantity"), filter.getMaxQuantity()));
            }

            if (filter.getMinValue() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("totalValue"), filter.getMinValue()));
            }

            if (filter.getMaxValue() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("totalValue"), filter.getMaxValue()));
            }

            if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
                String search = "%" + filter.getSearchQuery().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("product").get("name")), search);
                Predicate skuMatch = cb.like(cb.lower(root.get("product").get("sku")), search);
                predicates.add(cb.or(nameMatch, skuMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Inventory> hasProductId(String productId) {
        return (root, query, cb) -> cb.equal(root.get("product").get("id"), productId);
    }

    public static Specification<Inventory> hasWarehouseId(String warehouseId) {
        return (root, query, cb) -> cb.equal(root.get("warehouse").get("id"), warehouseId);
    }

    public static Specification<Inventory> hasStatus(Inventory.StockStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Inventory> isLowStock() {
        return (root, query, cb) -> cb.equal(root.get("status"), Inventory.StockStatus.LOW_STOCK);
    }

    public static Specification<Inventory> isOutOfStock() {
        return (root, query, cb) -> cb.equal(root.get("status"), Inventory.StockStatus.OUT_OF_STOCK);
    }
}
